package com.loopy.core.planning;

import com.loopy.core.TypeToken;
import com.loopy.core.action.ActionDefinition;
import com.loopy.core.agent.GoalDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class PlanTest {

    private static GoalDefinition goal() {
        return new GoalDefinition("g", "goal", Set.of(), TypeToken.of(Object.class), 1.0);
    }

    private static ActionDefinition action(String name, double cost) {
        return new ActionDefinition(name, name, Set.of(), Set.of(), Set.of(), Set.of(), cost, false);
    }

    @Test
    void isComplete_whenEmpty() {
        var plan = new Plan(List.of(), goal());
        assertThat(plan.isComplete()).isTrue();
        assertThat(plan.stepCount()).isEqualTo(0);
    }

    @Test
    void isComplete_whenStepsRemain() {
        var plan = new Plan(List.of(action("a", 0.5)), goal());
        assertThat(plan.isComplete()).isFalse();
        assertThat(plan.stepCount()).isEqualTo(1);
    }

    @Test
    void nextStep_returnsFirstAction() {
        var a1 = action("first", 0.3);
        var a2 = action("second", 0.5);
        var plan = new Plan(List.of(a1, a2), goal());
        assertThat(plan.nextStep()).isEqualTo(a1);
    }

    @Test
    void nextStep_throwsWhenComplete() {
        var plan = new Plan(List.of(), goal());
        assertThatIllegalStateException().isThrownBy(plan::nextStep);
    }

    @Test
    void withoutFirstStep_removesFirstAction() {
        var a1 = action("first", 0.3);
        var a2 = action("second", 0.5);
        var plan = new Plan(List.of(a1, a2), goal());
        var remaining = plan.withoutFirstStep();
        assertThat(remaining.steps()).containsExactly(a2);
        assertThat(remaining.stepCount()).isEqualTo(1);
    }

    @Test
    void withoutFirstStep_throwsWhenComplete() {
        var plan = new Plan(List.of(), goal());
        assertThatIllegalStateException().isThrownBy(plan::withoutFirstStep);
    }

    @Test
    void totalCost_sumsAllSteps() {
        var plan = new Plan(List.of(action("a", 0.3), action("b", 0.5)), goal());
        assertThat(plan.totalCost(Beliefs.empty())).isCloseTo(0.8, within(0.001));
    }

    @Test
    void netValue_isGoalValueMinusCost() {
        var plan = new Plan(List.of(action("a", 0.3)), goal());
        assertThat(plan.netValue(Beliefs.empty())).isCloseTo(0.7, within(0.001));
    }

    @Test
    void stepsAreImmutable() {
        var mutable = new java.util.ArrayList<ActionDefinition>();
        mutable.add(action("a", 0.1));
        var plan = new Plan(mutable, goal());
        mutable.add(action("b", 0.2));
        assertThat(plan.stepCount()).isEqualTo(1);
    }
}
