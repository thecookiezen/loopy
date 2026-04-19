package com.loopy.core.condition;

/**
 * What an action changes when executed.
 * {@link Establishes} the condition becomes satisfied
 * {@link Revokes} the condition becomes unsatisfied
 */
public sealed interface Effect {

    Condition condition();

    record Establishes(Condition condition) implements Effect {
    }

    record Revokes(Condition condition) implements Effect {
    }

    static Effect establishes(Condition condition) {
        return new Establishes(condition);
    }

    static Effect revokes(Condition condition) {
        return new Revokes(condition);
    }
}
