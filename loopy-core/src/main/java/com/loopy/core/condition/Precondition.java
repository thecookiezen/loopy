package com.loopy.core.condition;

/**
 * What must hold for an action to be applicable or a goal to be achieved.
 * {@link Requires} - the condition must be satisfied
 * {@link Forbids} - the condition must NOT be satisfied
 */
public sealed interface Precondition {

    Condition condition();

    record Requires(Condition condition) implements Precondition {
    }

    record Forbids(Condition condition) implements Precondition {
    }

    static Precondition requires(Condition condition) {
        return new Requires(condition);
    }

    static Precondition forbids(Condition condition) {
        return new Forbids(condition);
    }
}
