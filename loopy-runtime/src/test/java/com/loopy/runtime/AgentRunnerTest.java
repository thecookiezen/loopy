package com.loopy.runtime;

import com.loopy.core.TokenUsage;
import com.loopy.core.TypeToken;
import com.loopy.core.action.ActionBinding;
import com.loopy.core.action.ActionDefinition;
import com.loopy.core.action.ActionExecutor;
import com.loopy.core.action.ActionResult;
import com.loopy.core.agent.AgentDefinition;
import com.loopy.core.agent.AgentLifecycleEvent;
import com.loopy.core.agent.GoalDefinition;
import com.loopy.core.agent.LifecycleListener;
import com.loopy.core.agent.SupervisionStrategy;
import com.loopy.core.condition.Condition;
import com.loopy.core.condition.Precondition;
import com.loopy.core.llm.ChatMessage;
import com.loopy.core.llm.LlmClient;
import com.loopy.core.llm.LlmObjectResult;
import com.loopy.core.llm.LlmRequest;
import com.loopy.core.llm.LlmResponse;
import com.loopy.core.mailbox.ImmutableMailbox;
import com.loopy.core.planning.DefaultBeliefDeriver;
import com.loopy.core.planning.GoapPlanner;
import com.loopy.core.tool.ToolRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class AgentRunnerTest {

    record Input(String text) {}
    record Output(String result) {}

    private static final TypeToken<Output> OUTPUT_TYPE = TypeToken.of(Output.class);
    private static final TypeToken<Input> INPUT_TYPE = TypeToken.of(Input.class);

    private static final LlmClient llm = new StubLlmClient();
    private static final RunOptions options = new RunOptions(10, 5, Duration.ofSeconds(30),
                List.of(), new SupervisionStrategy.Replan(), List.of());


    private AgentDefinition simpleAgent() {
        var action = new ActionDefinition(
                "transform", "Transforms input",
                Set.of(), Set.of(),
                Set.of(INPUT_TYPE),
                Set.of(OUTPUT_TYPE),
                0.3, false);
        var executor = (ActionExecutor) ctx -> {
            var input = ctx.input(Input.class);
            return ActionResult.Success.of(new Output("processed: " + input.text()));
        };
        var goal = new GoalDefinition("done", "Processing complete",
                Set.of(Precondition.requires(Condition.typePresent(OUTPUT_TYPE))),
                OUTPUT_TYPE, 1.0);
        return new AgentDefinition("test-agent", "Test agent",
                List.of(new ActionBinding(action, executor)),
                Set.of(goal), Set.of());
    }

    @Test
    void run_completesWhenGoalSatisfied() {
        var agent = simpleAgent();
        var goal = agent.goals().iterator().next();
        var mailbox = ImmutableMailbox.empty().post(new Input("hello"));
        
        var runner = new AgentRunner(List.of());
        var execution = runner.run(agent, goal, mailbox, llm,
                ToolRegistry.empty(), new GoapPlanner(),
                new DefaultBeliefDeriver(), options);

        assertThat(execution).isInstanceOf(AgentExecution.Completed.class);
        var completed = (AgentExecution.Completed) execution;
        assertThat(completed.result()).isInstanceOf(Output.class);
        assertThat(((Output) completed.result()).result()).isEqualTo("processed: hello");
        assertThat(completed.trace().steps()).hasSize(1);
    }

    @Test
    void run_emitsLifecycleEvents() {
        var events = new ArrayList<AgentLifecycleEvent>();
        LifecycleListener collector = events::add;

        var agent = simpleAgent();
        var goal = agent.goals().iterator().next();
        var mailbox = ImmutableMailbox.empty().post(new Input("hi"));
        
        var runner = new AgentRunner(List.of(collector));
        runner.run(agent, goal, mailbox, llm,
                ToolRegistry.empty(), new GoapPlanner(),
                new DefaultBeliefDeriver(), options);

        assertThat(events.stream()
                .anyMatch(e -> e instanceof AgentLifecycleEvent.Started)).isTrue();
        assertThat(events.stream()
                .anyMatch(e -> e instanceof AgentLifecycleEvent.ActionCompleted)).isTrue();
        assertThat(events.stream()
                .anyMatch(e -> e instanceof AgentLifecycleEvent.ProcessCompleted)).isTrue();
    }

    @Test
    void run_stuckWhenNoPlan() {
        var action = new ActionDefinition(
                "a", "a", Set.of(), Set.of(),
                Set.of(), Set.of(OUTPUT_TYPE),
                0.5, false);
        var executor = (com.loopy.core.action.ActionExecutor) ctx ->
                ActionResult.Success.of(new Output("x"));
        var goal = new GoalDefinition("g", "g",
                Set.of(Precondition.requires(Condition.custom("impossible"))),
                OUTPUT_TYPE, 1.0);
        var agent = new AgentDefinition("stuck-agent", "",
                List.of(new ActionBinding(action, executor)),
                Set.of(goal), Set.of());

        var mailbox = ImmutableMailbox.empty();
        
        var runner = new AgentRunner(List.of());
        var execution = runner.run(agent, agent.goals().iterator().next(),
                mailbox, llm, ToolRegistry.empty(), new GoapPlanner(),
                new DefaultBeliefDeriver(), options);

        assertThat(execution).isInstanceOf(AgentExecution.Stuck.class);
    }

    @Test
    void run_failsWithFailSupervision() {
        var action = new ActionDefinition(
                "explode", "boom", Set.of(), Set.of(),
                Set.of(INPUT_TYPE),
                Set.of(OUTPUT_TYPE),
                0.5, false);
        var executor = (com.loopy.core.action.ActionExecutor) ctx -> {
            throw new RuntimeException("boom");
        };
        var goal = new GoalDefinition("g", "g",
                Set.of(Precondition.requires(Condition.typePresent(OUTPUT_TYPE))),
                OUTPUT_TYPE, 1.0);
        var agent = new AgentDefinition("fail-agent", "",
                List.of(new ActionBinding(action, executor)),
                Set.of(goal), Set.of(), new SupervisionStrategy.Fail());

        var mailbox = ImmutableMailbox.empty().post(new Input("test"));
        
        var runner = new AgentRunner(List.of());
        var execution = runner.run(agent, agent.goals().iterator().next(),
                mailbox, llm, ToolRegistry.empty(), new GoapPlanner(),
                new DefaultBeliefDeriver(), options);

        assertThat(execution).isInstanceOf(AgentExecution.Failed.class);
    }

    @Test
    void run_singleUse_returnsRejectedOnSecondCall() {
        var agent = simpleAgent();
        var goal = agent.goals().iterator().next();
        var mailbox = ImmutableMailbox.empty().post(new Input("x"));
        
        var runner = new AgentRunner(List.of());
        runner.run(agent, goal, mailbox, llm, ToolRegistry.empty(),
                new GoapPlanner(), new DefaultBeliefDeriver(), options);

        var secondRun = runner.run(agent, goal, mailbox, llm,
                ToolRegistry.empty(), new GoapPlanner(),
                new DefaultBeliefDeriver(), options);

        assertThat(secondRun).isInstanceOf(AgentExecution.Rejected.class);
        assertThat(((AgentExecution.Rejected) secondRun).reason()).contains("single-use");
    }

    private static class StubLlmClient implements LlmClient {
        @Override
        public LlmResponse chat(LlmRequest request) {
            return new LlmResponse(
                    new ChatMessage.Assistant("stub"),
                    TokenUsage.zero(), "stub-model", "stop");
        }

        @Override
        public <T> LlmObjectResult<T> createObject(LlmRequest request, Class<T> outputType) {
            return new LlmObjectResult<>(null, TokenUsage.zero());
        }
    }
}
