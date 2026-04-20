package com.loopy.core.condition;

import com.loopy.core.TypeToken;
import com.loopy.core.mailbox.Mailbox;

/**
 * A proposition the planner can reason about symbolically.
 * Identity is structural (records), so the planner can match
 * an action's effect to another action's precondition by equality.
 *
 * Conditions come in two flavors:
 * {@link TypePresent} - automatically derived from mailbox type presence,
 *   with a built-in evaluator that checks the mailbox
 * {@link Custom} - user-defined, evaluated by a user-supplied
 *   {@link ConditionEvaluator} (defaults to undecidable)
 */
public sealed interface Condition {

    ConditionEvaluator defaultEvaluator();

    record TypePresent(TypeToken<?> type) implements Condition {
        @Override
        public String toString() {
            return "has:" + type.simpleName();
        }

        @Override
        public ConditionEvaluator defaultEvaluator() {
            return (Mailbox mailbox) -> mailbox.last(type).isPresent()
                    ? ConditionResult.satisfied()
                    : ConditionResult.unsatisfied();
        }
    }

    record Custom(String name) implements Condition {
        @Override
        public String toString() {
            return name;
        }

        @Override
        public ConditionEvaluator defaultEvaluator() {
            return (Mailbox mailbox) -> ConditionResult.undecidable("no evaluator for: " + name);
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
