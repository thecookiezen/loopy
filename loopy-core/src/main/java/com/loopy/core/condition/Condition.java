package com.loopy.core.condition;

import com.loopy.core.TypeToken;

/**
 * A proposition the planner can reason about symbolically.
 * Identity is structural (records), so the planner can match
 * an action's effect to another action's precondition by equality.
 *
 * Conditions come in two flavors:
 * {@link TypePresent} - automatically derived from mailbox type presence
 * {@link Custom} - user-defined, evaluated by a {@link ConditionEvaluator}
 */
public sealed interface Condition {

    record TypePresent(TypeToken<?> type) implements Condition {
        @Override
        public String toString() {
            return "has:" + type.simpleName();
        }
    }

    record Custom(String name) implements Condition {
        @Override
        public String toString() {
            return name;
        }
    }

    static Condition typePresent(TypeToken<?> type) {
        return new TypePresent(type);
    }

    static Condition typePresent(Class<?> type) {
        return new TypePresent(TypeToken.of(type));
    }

    static Custom custom(String name) {
        return new Custom(name);
    }
}
