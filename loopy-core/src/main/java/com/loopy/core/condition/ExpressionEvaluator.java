package com.loopy.core.condition;

import com.loopy.core.mailbox.Mailbox;

/**
 * Port for evaluating expression-language conditions.
 * Infrastructure adapters implement this for specific engines
 * (SpEL, MVEL, JEXL, etc.). The expression is evaluated against the
 * latest mailbox value of the given target type.
 */
@FunctionalInterface
public interface ExpressionEvaluator {

    /**
     * Evaluate an expression against the mailbox.
     *
     * @param expression the expression string
     * @param targetType the type of mailbox message to bind as the root context
     * @param mailbox    current mailbox state
     * @return satisfied, unsatisfied, or undecidable
     */
    ConditionResult evaluate(String expression, Class<?> targetType, Mailbox mailbox);
}
