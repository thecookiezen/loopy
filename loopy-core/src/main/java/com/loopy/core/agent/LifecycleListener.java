package com.loopy.core.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface LifecycleListener {

    static final Logger log = LoggerFactory.getLogger(LifecycleListener.class);

    void onEvent(AgentLifecycleEvent event);

    default LifecycleListener andThen(LifecycleListener other) {
        return event -> {
            this.onEvent(event);
            other.onEvent(event);
        };
    }

    static LifecycleListener logging() {
        return event -> {
            switch (event) {
                case AgentLifecycleEvent.Started e ->
                    log.info("[{}] Agent '{}' started toward goal '{}'", e.agentId(),
                            e.agent().name(), e.goal().name());
                case AgentLifecycleEvent.ActionStarted e ->
                    log.info("  → Action '{}' starting", e.action().name());
                case AgentLifecycleEvent.ActionCompleted e ->
                    log.info("  ✓ Action '{}' completed in {}ms", e.action().name(),
                            e.duration().toMillis());
                case AgentLifecycleEvent.ActionFailed e ->
                    log.warn("  ✗ Action '{}' failed: {} (strategy: {})",
                            e.action().name(), e.cause().getMessage(), e.appliedStrategy());
                case AgentLifecycleEvent.Replanned e ->
                    log.info("  ↻ Replanned: {} steps", e.newPlan().stepCount());
                case AgentLifecycleEvent.BehaviorChanged e ->
                    log.debug("  Behavior: {} → {}", e.from().getClass().getSimpleName(),
                            e.to().getClass().getSimpleName());
                case AgentLifecycleEvent.ProcessCompleted e ->
                    log.info("Agent completed with result: {}", e.result());
                case AgentLifecycleEvent.ProcessFailed e ->
                    log.error("Agent failed", e.cause());
                default -> {}
            }
        };
    }

    static LifecycleListener debug() {
        return event -> {
            switch (event) {
                case DebugEvent.LlmRequestSent e ->
                    log.debug("[LLM] Request sent to model '{}', messages: {}",
                            e.request().model(), e.request().messages().size());
                case DebugEvent.LlmResponseReceived e ->
                    log.debug("[LLM] Response from '{}', tokens: {}, duration: {}ms",
                            e.model(), e.response().usage().totalTokens(),
                            e.duration().toMillis());
                case DebugEvent.LlmObjectRequestSent e ->
                    log.debug("[LLM] Object request sent, outputType: {}, model: '{}'",
                            e.outputType().getSimpleName(), e.request().model());
                case DebugEvent.LlmObjectResultReceived e ->
                    log.debug("[LLM] Object result received, outputType: {}, tokens: {}, duration: {}ms",
                            e.outputType().getSimpleName(), e.result().usage().totalTokens(),
                            e.duration().toMillis());
                case DebugEvent.ToolCallExecuted e ->
                    log.debug("[TOOL] '{}' executed, duration: {}ms, result: {}",
                            e.request().functionName(), e.duration().toMillis(),
                            truncate(e.result().resultContent(), 500));
                case DebugEvent.ToolLoopIteration e ->
                    log.debug("[TOOL LOOP] Iteration {}/{}, messages: {}",
                            e.iteration(), e.maxIterations(), e.messages().size());
                case DebugEvent.ToolLoopCompleted e ->
                    log.debug("[TOOL LOOP] Completed, iterations: {}, tokens: {}",
                            e.totalIterations(), e.accumulatedUsage().totalTokens());
                case DebugEvent.PlanningStarted e ->
                    log.debug("[PLAN] Planning started, actions: {}, goal: '{}'",
                            e.actions().size(), e.goal().name());
                case DebugEvent.PlanningNodeExpanded e ->
                    log.debug("[PLAN] Node expanded, action: '{}', g: {}, h: {}",
                            e.actionApplied().name(), e.gScore(), e.hScore());
                case DebugEvent.PlanningCompleted e ->
                    log.debug("[PLAN] Planning completed, plan steps: {}, iterations: {}",
                            e.plan().stepCount(), e.iterations());
                case DebugEvent.PlanningFailed e ->
                    log.debug("[PLAN] Planning failed after {} iterations", e.iterations());
                case DebugEvent.OodaCycleStarted e ->
                    log.debug("[OODA] Cycle {}, mailbox: {}, beliefs: {}",
                            e.stepNumber(), e.mailbox().messages().size(), e.beliefs().satisfiedConditions().size());
                case DebugEvent.ActionInputResolved e ->
                    log.debug("[OODA] Action '{}' inputs resolved, count: {}",
                            e.action().name(), e.inputs().size());
                default -> {}
            }
        };
    }

    static LifecycleListener noop() {
        return event -> {};
    }

    static String truncate(String text, int maxLength) {
        if (text == null) return "null";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...[truncated]";
    }
}
