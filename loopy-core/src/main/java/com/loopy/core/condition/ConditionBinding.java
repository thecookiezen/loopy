package com.loopy.core.condition;

import java.util.function.Predicate;

import com.loopy.core.TypeToken;
import com.loopy.core.planning.BeliefDeriver;

/**
 * Binds a {@link Condition.Custom} to its evaluation logic.
 *
 * Three flavors:
 * {@link Custom} - opaque evaluator, full control over evaluation
 * {@link TypePredicate} - type-safe predicate over a mailbox value
 * {@link Expression} - expression string evaluated by an {@link ExpressionEvaluator}
 * 
 * {@link Condition.TypePresent} conditions don't need bindings, they are
 * evaluated automatically by {@link BeliefDeriver}.
 */
public sealed interface ConditionBinding {

    Condition.Custom condition();

    ConditionEvaluator evaluator();

    record Custom(Condition.Custom condition, ConditionEvaluator evaluator) implements ConditionBinding {
    }

    /**
     * Type-safe predicate over the latest mailbox value of a given type.
     * If no message of that type exists, the condition is undecidable.
     */
    record TypePredicate<T>(Condition.Custom condition, TypeToken<T> targetType, Predicate<T> test)
            implements ConditionBinding {

        @Override
        public ConditionEvaluator evaluator() {
            return mailbox -> mailbox.last(targetType)
                    .map(value -> test.test(value)
                            ? ConditionResult.satisfied()
                            : ConditionResult.unsatisfied())
                    .orElse(ConditionResult.undecidable(
                            "No message of type " + targetType.simpleName()));
        }
    }

    /**
     * Expression-based condition evaluated by an {@link ExpressionEvaluator}
     * engine. The expression is evaluated against the latest mailbox value of
     * {@code targetType}.
     */
    record Expression(Condition.Custom condition, Class<?> targetType, String expression,
            ExpressionEvaluator engine) implements ConditionBinding {

        @Override
        public ConditionEvaluator evaluator() {
            return mailbox -> engine.evaluate(expression, targetType, mailbox);
        }
    }

    static ConditionBinding custom(String name, ConditionEvaluator evaluator) {
        return new Custom(Condition.custom(name), evaluator);
    }

    static ConditionBinding custom(Condition.Custom condition, ConditionEvaluator evaluator) {
        return new Custom(condition, evaluator);
    }

    static <T> ConditionBinding typePredicate(String name, TypeToken<T> targetType, Predicate<T> test) {
        return new TypePredicate<>(Condition.custom(name), targetType, test);
    }

    static <T> ConditionBinding typePredicate(String name, TypeToken<T> targetType) {
        return new TypePredicate<>(Condition.custom(name), targetType, t -> true);
    }

    static ConditionBinding expression(String name, Class<?> targetType, String expression,
            ExpressionEvaluator engine) {
        return new Expression(Condition.custom(name), targetType, expression, engine);
    }
}
