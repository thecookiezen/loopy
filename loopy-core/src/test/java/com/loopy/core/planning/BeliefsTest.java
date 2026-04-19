package com.loopy.core.planning;

import com.loopy.core.condition.Condition;
import com.loopy.core.condition.Effect;
import com.loopy.core.condition.Precondition;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class BeliefsTest {

    @Test
    void empty_hasNoConditions() {
        assertThat(Beliefs.empty().satisfiedConditions()).isEmpty();
    }

    @Test
    void with_addsCondition() {
        var cond = Condition.custom("x");
        var beliefs = Beliefs.empty().with(cond);
        assertThat(beliefs.holds(cond)).isTrue();
    }

    @Test
    void without_removesCondition() {
        var cond = Condition.custom("x");
        var beliefs = Beliefs.empty().with(cond).without(cond);
        assertThat(beliefs.holds(cond)).isFalse();
    }

    @Test
    void holds_returnsFalseForAbsentCondition() {
        assertThat(Beliefs.empty().holds(Condition.custom("missing"))).isFalse();
    }

    @Test
    void satisfies_allRequiredPresent() {
        var a = Condition.custom("a");
        var b = Condition.custom("b");
        var beliefs = Beliefs.empty().with(a).with(b);
        assertThat(beliefs.satisfies(Set.of(
                Precondition.requires(a),
                Precondition.requires(b)))).isTrue();
    }

    @Test
    void satisfies_missingRequired_returnsFalse() {
        var a = Condition.custom("a");
        var beliefs = Beliefs.empty().with(a);
        assertThat(beliefs.satisfies(Set.of(
                Precondition.requires(a),
                Precondition.requires(Condition.custom("b"))))).isFalse();
    }

    @Test
    void isSatisfied_forbidsConditionMet_returnsFalse() {
        var cond = Condition.custom("x");
        var beliefs = Beliefs.empty().with(cond);
        assertThat(beliefs.isSatisfied(Precondition.forbids(cond))).isFalse();
    }

    @Test
    void isSatisfied_forbidsConditionAbsent_returnsTrue() {
        var beliefs = Beliefs.empty();
        assertThat(beliefs.isSatisfied(Precondition.forbids(Condition.custom("x")))).isTrue();
    }

    @Test
    void applyEffects_establishes() {
        var cond = Condition.custom("x");
        var beliefs = Beliefs.empty().applyEffects(Set.of(Effect.establishes(cond)));
        assertThat(beliefs.holds(cond)).isTrue();
    }

    @Test
    void applyEffects_revokes() {
        var cond = Condition.custom("x");
        var beliefs = Beliefs.empty().with(cond).applyEffects(Set.of(Effect.revokes(cond)));
        assertThat(beliefs.holds(cond)).isFalse();
    }

    @Test
    void applyEffects_mixedEstablishAndRevoke() {
        var a = Condition.custom("a");
        var b = Condition.custom("b");
        var beliefs = Beliefs.empty().with(a).with(b)
                .applyEffects(Set.of(Effect.revokes(a), Effect.establishes(Condition.custom("c"))));
        assertThat(beliefs.holds(a)).isFalse();
        assertThat(beliefs.holds(b)).isTrue();
        assertThat(beliefs.holds(Condition.custom("c"))).isTrue();
    }
}
