package com.loopy.core.condition;

/**
 * Result of evaluating a condition against the current mailbox state.
 * A condition is either deterministically satisfied/unsatisfied, or undecidable
 * when the mailbox lacks sufficient information.
 */
public sealed interface ConditionResult {

    record Satisfied() implements ConditionResult {
    }

    record Unsatisfied() implements ConditionResult {
    }

    record Undecidable(String reason) implements ConditionResult {
    }

    static ConditionResult satisfied() {
        return new Satisfied();
    }

    static ConditionResult unsatisfied() {
        return new Unsatisfied();
    }

    static ConditionResult undecidable(String reason) {
        return new Undecidable(reason);
    }
}
