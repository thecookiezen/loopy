package com.loopy.runtime;

import com.loopy.core.action.ActionResult;
import com.loopy.core.TokenUsage;
import com.loopy.core.agent.AgentLifecycleEvent;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Record of an agent execution, capturing every step, event,
 * token usage, cost, and duration.
 *
 * Use {@link #costSummary()} for a human-readable summary suitable for
 * logging or CLI output.
 *
 * @param steps         ordered list of action execution records
 * @param events        all lifecycle events emitted during execution
 * @param totalTokens   aggregated token usage across all LLM calls
 * @param totalCost     estimated cost in USD based on the configured pricing models
 * @param modelsUsed    set of model identifiers used during execution
 * @param totalDuration wall-clock duration from start to completion
 */
public record ExecutionTrace(
        List<StepRecord> steps,
        List<AgentLifecycleEvent> events,
        TokenUsage totalTokens,
        double totalCost,
        Set<String> modelsUsed,
        Duration totalDuration) {
    public ExecutionTrace {
        steps = Collections.unmodifiableList(List.copyOf(steps));
        events = Collections.unmodifiableList(List.copyOf(events));
        modelsUsed = Collections.unmodifiableSet(Set.copyOf(modelsUsed));
    }

    /**
     * Record of a single action execution step.
     *
     * @param actionName the name of the action that was executed
     * @param duration   the wall-clock duration of the execution
     * @param result     the action result (success or failure)
     * @param usage      token usage consumed by this action's LLM calls
     */
    public record StepRecord(
            String actionName,
            Duration duration,
            ActionResult result,
            TokenUsage usage) {
    }

    /**
     * Return a formatted multi-line summary of the execution cost and statistics.
     *
     * @return a human-readable cost summary string
     */
    public String costSummary() {
        return """
                === Execution Summary ===
                Steps:       %d
                Total tokens: %d (prompt: %d, completion: %d)
                Total cost:  $%.6f
                Models used: %s
                Duration:    %s
                """.formatted(
                steps.size(),
                totalTokens.totalTokens(), totalTokens.promptTokens(), totalTokens.completionTokens(),
                totalCost,
                modelsUsed,
                totalDuration);
    }

    /**
     * Return an empty execution trace with zero usage and no steps.
     *
     * @return an empty trace
     */
    public static ExecutionTrace empty() {
        return new ExecutionTrace(List.of(), List.of(), TokenUsage.zero(), 0.0, Set.of(), Duration.ZERO);
    }
}
