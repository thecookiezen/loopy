package com.loopy.core.condition;

import com.loopy.core.TypeToken;
import com.loopy.core.mailbox.ImmutableMailbox;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ConditionBindingTest {

    @Test
    void customBinding_delegatesToEvaluator() {
        var mailbox = ImmutableMailbox.empty().post("hello");
        var binding = ConditionBinding.custom("hasMessage",
                m -> m.last(TypeToken.of(String.class)).isPresent()
                        ? ConditionResult.satisfied()
                        : ConditionResult.unsatisfied());

        assertThat(binding.condition()).isEqualTo(Condition.custom("hasMessage"));
        assertThat(binding.evaluator().evaluate(mailbox)).isInstanceOf(ConditionResult.Satisfied.class);
    }

    record Order(String id, double total) {}

    @Test
    void typePredicate_satisfied_whenPredicateMatches() {
        var mailbox = ImmutableMailbox.empty().post(new Order("A1", 800.0));
        var binding = ConditionBinding.typePredicate(
                "orderIsLarge", TypeToken.of(Order.class), order -> order.total() > 500);

        var result = binding.evaluator().evaluate(mailbox);

        assertThat(result).isInstanceOf(ConditionResult.Satisfied.class);
    }

    @Test
    void typePredicate_unsatisfied_whenPredicateDoesNotMatch() {
        var mailbox = ImmutableMailbox.empty().post(new Order("A2", 100.0));
        var binding = ConditionBinding.typePredicate(
                "orderIsLarge", TypeToken.of(Order.class), order -> order.total() > 500);

        var result = binding.evaluator().evaluate(mailbox);

        assertThat(result).isInstanceOf(ConditionResult.Unsatisfied.class);
    }

    @Test
    void typePredicate_undecidable_whenNoMessageOfType() {
        var mailbox = ImmutableMailbox.empty().post("not an order");
        var binding = ConditionBinding.typePredicate(
                "orderIsLarge", TypeToken.of(Order.class), order -> order.total() > 500);

        var result = binding.evaluator().evaluate(mailbox);

        assertThat(result).isInstanceOf(ConditionResult.Undecidable.class);
    }

    @Test
    void typePredicate_conditionIsCustomWithGivenName() {
        var binding = ConditionBinding.typePredicate(
                "orderIsLarge", TypeToken.of(Order.class), order -> order.total() > 500);

        assertThat(binding.condition()).isEqualTo(Condition.custom("orderIsLarge"));
    }

    @Test
    void expression_delegatesToEngine() {
        var mailbox = ImmutableMailbox.empty().post(new Order("A3", 999.0));

        ExpressionEvaluator stubEngine = (expression, targetType, m) -> {
            var value = m.last(TypeToken.of(targetType));
            if (value.isEmpty()) {
                return ConditionResult.undecidable("no value");
            }
            return expression.contains(">")
                    ? ConditionResult.satisfied()
                    : ConditionResult.unsatisfied();
        };

        var binding = ConditionBinding.expression(
                "orderIsLarge", Order.class, "total > 500", stubEngine);

        var result = binding.evaluator().evaluate(mailbox);

        assertThat(result).isInstanceOf(ConditionResult.Satisfied.class);
        assertThat(binding.condition()).isEqualTo(Condition.custom("orderIsLarge"));
    }

    @Test
    void expression_undecidable_whenEngineReturnsUndecidable() {
        var mailbox = ImmutableMailbox.empty();

        ExpressionEvaluator stubEngine = (expression, targetType, m) ->
                ConditionResult.undecidable("no data");

        var binding = ConditionBinding.expression(
                "orderIsLarge", Order.class, "total > 500", stubEngine);

        var result = binding.evaluator().evaluate(mailbox);

        assertThat(result).isInstanceOf(ConditionResult.Undecidable.class);
    }
}
