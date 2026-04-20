package com.loopy.runtime;

import com.loopy.core.TokenUsage;
import com.loopy.core.action.ActionBinding;
import com.loopy.core.action.ActionContext;
import com.loopy.core.action.ActionDefinition;
import com.loopy.core.action.ActionResult;
import com.loopy.core.agent.*;
import com.loopy.core.mailbox.InMemoryMailbox;
import com.loopy.core.mailbox.Mailbox;
import com.loopy.core.planning.BeliefDeriver;
import com.loopy.core.planning.Planner;
import com.loopy.core.tool.ToolRegistry;
import com.loopy.core.llm.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The core execution engine. Supports behavior switching, 
 * supervision, message passing, virtual threads, lifecycle events.
 *
 * Single-use per run - create a new instance for each agent execution.
 */
public final class AgentRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AgentRunner.class);

    private final AgentId agentId;
    private LifecycleListener listener;
    private final List<AgentLifecycleEvent> events = new ArrayList<>();
    private final List<ExecutionTrace.StepRecord> steps = new ArrayList<>();
    private final Set<String> modelsUsed = new HashSet<>();
    private TokenUsage totalUsage = TokenUsage.zero();
    private AgentBehavior behavior = null;
    private boolean executed = false;

    public AgentRunner() {
        this.agentId = AgentId.generate();
    }

    /**
     * Returns the unique identifier for this agent execution instance.
     */
    public AgentId agentId() {
        return agentId;
    }

    /**
     * Run an agent to completion using the OODA loop with action supervision.
     *
     */
    public AgentExecution run(
            AgentDefinition agent,
            GoalDefinition goal,
            Mailbox initialState,
            LlmClient llmClient,
            ToolRegistry tools,
            Planner planner,
            BeliefDeriver beliefDeriver,
            RunOptions options) {
        if (executed) {
            return new AgentExecution.Rejected(
                    "AgentRunner is single-use - create a new instance for each run");
        }
        executed = true;
        this.listener = compositeListener(options.listeners());

        var start = Instant.now();
        var mailbox = InMemoryMailbox.from(initialState);

        emit(new AgentLifecycleEvent.Started(agentId, agent, goal, Instant.now()));

        for (int step = 0; step < options.maxSteps(); step++) {
            // OBSERVE: derive beliefs from mailbox
            var beliefs = beliefDeriver.derive(mailbox, agent);

            // Check if goal already achieved
            if (beliefs.satisfies(goal.preconditions())) {
                var result = mailbox.last(goal.outputType()).orElse(null);
                emitBehaviorChange(new AgentBehavior.Completed(result));

                var trace = buildTrace(start, options);
                emit(new AgentLifecycleEvent.ProcessCompleted(result, Instant.now()));

                return new AgentExecution.Completed(result, mailbox, trace);
            }

            // ORIENT: plan via GOAP
            var actionDefs = agent.actions().stream()
                    .map(ActionBinding::definition)
                    .collect(Collectors.toSet());
            var planResult = planner.plan(beliefs, actionDefs, goal);

            if (planResult.isEmpty()) {
                emitBehaviorChange(new AgentBehavior.Stuck("No plan found from current state"));
                var trace = buildTrace(start, options);
                return new AgentExecution.Stuck(mailbox, trace, "No plan found");
            }

            var plan = planResult.get();
            if (plan.isComplete()) {
                var result = mailbox.last(goal.outputType()).orElse(null);
                var trace = buildTrace(start, options);
                return new AgentExecution.Completed(result, mailbox, trace);
            }

            var newBehavior = new AgentBehavior.Executing(plan, step);
            emit(new AgentLifecycleEvent.Replanned(plan, beliefs, Instant.now()));
            emitBehaviorChange(newBehavior);
            behavior = newBehavior;

            // DECIDE: pick first action
            var nextActionDef = plan.nextStep();
            var bindingOpt = agent.actions().stream()
                    .filter(b -> b.definition().equals(nextActionDef))
                    .findFirst();

            if (bindingOpt.isEmpty()) {
                var trace = buildTrace(start, options);
                return new AgentExecution.Failed(
                        new IllegalStateException("No binding found for action: " + nextActionDef.name()),
                        trace);
            }
            var binding = bindingOpt.get();

            // ACT: execute on virtual thread
            emit(new AgentLifecycleEvent.ActionStarted(nextActionDef, mailbox, Instant.now()));

            var actionStart = Instant.now();
            ActionResult actionResult;
            try {
                var context = new ActionContext(mailbox, llmClient, tools, agent, nextActionDef,
                        options.maxToolLoopIterations());
                var futureResult = new CompletableFuture<ActionResult>();
                Thread.ofVirtual()
                        .name("action-" + nextActionDef.name())
                        .start(() -> {
                            try {
                                futureResult.complete(binding.executor().execute(context));
                            } catch (Exception e) {
                                futureResult.complete(new ActionResult.Failure(e.getMessage(), e));
                            }
                        });
                actionResult = futureResult.get(options.timeout().toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                actionResult = new ActionResult.Failure(e.getMessage(), e);
            }
            var actionDuration = Duration.between(actionStart, Instant.now());

            switch (actionResult) {
                case ActionResult.Success success -> {
                    emit(new AgentLifecycleEvent.ActionCompleted(nextActionDef, actionResult, actionDuration, Instant.now()));

                    if (success.output() != null) {
                        mailbox.post(success.output());
                    }
                    totalUsage = totalUsage.plus(success.usage());
                    steps.add(new ExecutionTrace.StepRecord(nextActionDef.name(), actionDuration, actionResult, success.usage()));
                }
                case ActionResult.Failure failure -> {
                    var supervision = options.defaultSupervision();
                    emit(new AgentLifecycleEvent.ActionFailed(nextActionDef, failure.cause(), supervision, Instant.now()));
                    steps.add(new ExecutionTrace.StepRecord(nextActionDef.name(), actionDuration, actionResult, TokenUsage.zero()));

                    switch (supervision) {
                        case SupervisionStrategy.Fail _ -> {
                            var trace = buildTrace(start, options);
                            emit(new AgentLifecycleEvent.ProcessFailed(failure.cause(), Instant.now()));
                            return new AgentExecution.Failed(failure.cause(), trace);
                        }
                        case SupervisionStrategy.Replan _ -> {
                            LOG.info("Replanning after action '{}' failed", nextActionDef.name());
                            // Continue loop - will replan on next iteration //todo
                        }
                        case SupervisionStrategy.Retry retry -> {
                            LOG.info("Retrying action '{}' (max attempts: {})", nextActionDef.name(),
                                    retry.maxAttempts());
                            // Just continue to next loop iteration (replan)
                        }
                        case SupervisionStrategy.Escalate escalate -> {
                            LOG.info("Escalating to LLM for decision on failed action '{}'", nextActionDef.name());
                            // fall through to replan //todo
                        }
                    }
                }
            }
        }

        // Max steps exceeded
        var trace = buildTrace(start, options);
        return new AgentExecution.Stuck(mailbox, trace, "Max steps (" + options.maxSteps() + ") exceeded");
    }

    private void emit(AgentLifecycleEvent event) {
        events.add(event);
        listener.onEvent(event);
    }

    private void emitBehaviorChange(AgentBehavior to) {
        if (behavior != null && !behavior.equals(to)) {
            emit(new AgentLifecycleEvent.BehaviorChanged(behavior, to, Instant.now()));
        }
    }

    private ExecutionTrace buildTrace(Instant start, RunOptions options) {
        double totalCost = options.pricingModels().stream()
                .filter(pm -> modelsUsed.contains(pm.modelName()))
                .mapToDouble(pm -> pm.costOf(totalUsage))
                .sum();
        if (modelsUsed.isEmpty() && !options.pricingModels().isEmpty()) {
            totalCost = options.pricingModels().getFirst().costOf(totalUsage);
        }
        return new ExecutionTrace(steps, events, totalUsage, totalCost, modelsUsed,
                Duration.between(start, Instant.now()));
    }

    private static LifecycleListener compositeListener(List<LifecycleListener> listeners) {
        return listeners.stream()
                .reduce(LifecycleListener.noop(), LifecycleListener::andThen);
    }
}
