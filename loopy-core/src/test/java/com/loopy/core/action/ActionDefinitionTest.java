package com.loopy.core.action;

import com.loopy.core.TypeToken;
import com.loopy.core.condition.Condition;
import com.loopy.core.condition.Effect;
import com.loopy.core.condition.Precondition;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class ActionDefinitionTest {

    @Test
    void costOutOfRange_throws() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ActionDefinition("a", "a", Set.of(), Set.of(),
                        Set.of(), Set.of(), -0.1, false));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ActionDefinition("a", "a", Set.of(), Set.of(),
                        Set.of(), Set.of(), 1.5, false));
    }

    @Test
    void validCost_bounds() {
        var zero = new ActionDefinition("a", "a", Set.of(), Set.of(),
                Set.of(), Set.of(), 0.0, false);
        var one = new ActionDefinition("b", "b", Set.of(), Set.of(),
                Set.of(), Set.of(), 1.0, false);
        assertThat(zero.cost()).isEqualTo(0.0);
        assertThat(one.cost()).isEqualTo(1.0);
    }

    @Test
    void inputTypes_autoGeneratePreconditions() {
        var action = new ActionDefinition("a", "a", Set.of(), Set.of(),
                Set.of(TypeToken.of(String.class)), Set.of(), 0.5, false);

        var expectedPrecond = Precondition.requires(Condition.typePresent(String.class));
        assertThat(action.preconditions()).anyMatch(p ->
                p instanceof Precondition.Requires && p.condition().equals(expectedPrecond.condition()));
    }

    @Test
    void outputTypes_autoGenerateEffects() {
        var action = new ActionDefinition("a", "a", Set.of(), Set.of(),
                Set.of(), Set.of(TypeToken.of(Integer.class)), 0.5, false);

        var expectedEffect = Effect.establishes(Condition.typePresent(Integer.class));
        assertThat(action.effects()).contains(expectedEffect);
    }

    @Test
    void setsAreImmutable() {
        var action = new ActionDefinition("a", "a", Set.of(), Set.of(),
                Set.of(TypeToken.of(String.class)), Set.of(TypeToken.of(Integer.class)), 0.5, false);
        assertThatThrownBy(() -> action.preconditions().add(
                Precondition.requires(Condition.custom("x"))))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
