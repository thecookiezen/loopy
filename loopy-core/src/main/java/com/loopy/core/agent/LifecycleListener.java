package com.loopy.core.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observes {@link AgentLifecycleEvent} instances emitted during agent execution.
 *
 * Implementations can monitor agent progress, implement human-in-the-loop
 * handoffs, record execution traces, or trigger side effects. Multiple listeners
 * can be composed using {@link #andThen(LifecycleListener)}.
 *
 * @see AgentLifecycleEvent
 */
@FunctionalInterface
public interface LifecycleListener {

    /**
     * Handle a lifecycle event emitted during agent execution.
     *
     * @param event the event to process
     */
    void onEvent(AgentLifecycleEvent event);

    /**
     * Compose this listener with another, executing this one first.
     *
     * @param other the listener to chain after this one
     * @return a composite listener that invokes both in order
     */
    default LifecycleListener andThen(LifecycleListener other) {
        return event -> {
            this.onEvent(event);
            other.onEvent(event);
        };
    }

    /**
     * Returns a listener that logs every lifecycle event.
     *
     * @return a logging listener
     */
    static LifecycleListener logging() {
        Logger log = LoggerFactory.getLogger(LifecycleListener.class);
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
            }
            ;
        };
    }

    /**
     * Returns a listener that silently discards all events.
     *
     * @return a no-op listener
     */
    static LifecycleListener noop() {
        return event -> {
        };
    }
}
