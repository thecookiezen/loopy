package com.loopy.core.agent;

import java.time.Duration;

/**
 * Decides what to do when an action fails.
 */
public sealed interface SupervisionStrategy {
    /** Retry the failed action up to N times with backoff */
    record Retry(int maxAttempts, Duration backoff) implements SupervisionStrategy {
    }

    /** Skip the failed action and re-plan toward the goal from current state */
    record Replan() implements SupervisionStrategy {
    }

    /** Fail the entire agent process immediately */
    record Fail() implements SupervisionStrategy {
    }

    /** Ask the LLM to decide what to do (self-healing agent) */
    record Escalate(String prompt) implements SupervisionStrategy {
    }
}
