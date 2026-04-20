package com.loopy.runtime;

import com.loopy.core.agent.LifecycleListener;
import com.loopy.core.agent.SupervisionStrategy;
import com.loopy.core.llm.PricingModel;

import java.time.Duration;
import java.util.List;

/**
 * Configuration options for a single {@link AgentRunner} execution.
 *
 * The single source of truth for execution boundaries, cost estimation,
 * supervision, and observability of an agent run.
 *
 * @param maxSteps              maximum number of OODA loop iterations
 * @param maxToolLoopIterations maximum number of tool-calling iterations per LLM call,
 *                              passed to {@link com.loopy.core.action.ActionContext} for use
 *                              with {@link ToolLoop}
 * @param timeout               wall-clock timeout per action execution
 * @param pricingModels         pricing models used for cost estimation
 * @param defaultSupervision    the default strategy when an action fails;
 *                              overrides the agent's built-in supervision for this run
 * @param listeners             lifecycle listeners to observe execution events
 */
public record RunOptions(
                int maxSteps,
                int maxToolLoopIterations,
                Duration timeout,
                List<PricingModel> pricingModels,
                SupervisionStrategy defaultSupervision,
                List<LifecycleListener> listeners) {

    /**
     * Return sensible default run options:
     * 20 max steps, 10 max tool loop iterations, 5-minute timeout,
     * standard GPT pricing models, replan supervision, and logging listener.
     *
     * @return default run options
     */
    public static RunOptions defaults() {
        return new RunOptions(
                        20,
                        10,
                        Duration.ofMinutes(5),
                        List.of(PricingModel.GPT_5_4, PricingModel.GPT_5_4_MINI,
                                        PricingModel.GPT_5_4_NANO),
                        new SupervisionStrategy.Replan(),
                        List.of(LifecycleListener.logging()));
    }
}
