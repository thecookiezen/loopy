package com.loopy.core.condition;

/**
 * What must hold for an action to be applicable or a goal to be achieved.
 * Each precondition carries an evaluator that determines the condition's
 * truth against the mailbox. For {@link Condition.TypePresent} conditions
 * this is automatic; for {@link Condition.Custom} conditions the default
 * is undecidable (override with an explicit evaluator).
 *
 * {@link Requires} - the condition must be satisfied
 * {@link Forbids} - the condition must NOT be satisfied
 */
public sealed interface Precondition {

    Condition condition();

    ConditionEvaluator evaluator();

    record Requires(Condition condition, ConditionEvaluator evaluator) implements Precondition {
    }

    record Forbids(Condition condition, ConditionEvaluator evaluator) implements Precondition {
    }

    static Precondition requires(Condition condition) {
        return new Requires(condition, condition.defaultEvaluator());
    }

    static Precondition requires(Condition condition, ConditionEvaluator evaluator) {
        return new Requires(condition, evaluator);
    }

    static Precondition forbids(Condition condition) {
        return new Forbids(condition, condition.defaultEvaluator());
    }

    static Precondition forbids(Condition condition, ConditionEvaluator evaluator) {
        return new Forbids(condition, evaluator);
    }
}
