package com.loopy.core.condition;

import com.loopy.core.mailbox.Mailbox;

/**
 * Evaluates a named condition against the current mailbox state.
 */
@FunctionalInterface
public interface ConditionEvaluator {
    ConditionResult evaluate(Mailbox mailbox);
}
