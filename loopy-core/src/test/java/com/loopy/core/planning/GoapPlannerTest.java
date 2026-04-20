package com.loopy.core.planning;

import com.loopy.core.TypeToken;
import com.loopy.core.action.ActionDefinition;
import com.loopy.core.agent.GoalDefinition;
import com.loopy.core.condition.Condition;
import com.loopy.core.condition.Effect;
import com.loopy.core.condition.Precondition;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class GoapPlannerTest {

    private final Planner planner = new GoapPlanner();

    @Test
    void goalAlreadySatisfied_returnsEmptyPlan() {
        var done = Condition.custom("done");
        var state = Beliefs.empty().with(done);
        var goal = goal("finish", Precondition.requires(done));

        var plan = planner.plan(state, Set.of(), goal);

        assertThat(plan).isPresent();
        assertThat(plan.get().isComplete()).isTrue();
    }

    @Test
    void singleAction_achievesGoal() {
        var hasCake = Condition.custom("hasCake");
        var state = Beliefs.empty();
        var bake = action("bake", Set.of(), Set.of(Effect.establishes(hasCake)), 0.5);
        var goal = goal("getCake", Precondition.requires(hasCake));

        var plan = planner.plan(state, Set.of(bake), goal);

        assertThat(plan).isPresent();
        assertThat(plan.get().steps()).extracting(ActionDefinition::name).containsExactly("bake");
    }

    @Test
    void twoStepChain_orderedCorrectly() {
        var hasBatter = Condition.custom("hasBatter");
        var hasCake = Condition.custom("hasCake");
        var state = Beliefs.empty();
        var mix = action("mix", Set.of(), Set.of(Effect.establishes(hasBatter)), 0.3);
        var bake = action("bake",
                Set.of(Precondition.requires(hasBatter)),
                Set.of(Effect.establishes(hasCake)), 0.5);
        var goal = goal("getCake", Precondition.requires(hasCake));

        var plan = planner.plan(state, Set.of(mix, bake), goal);

        assertThat(plan).isPresent();
        assertThat(plan.get().steps()).extracting(ActionDefinition::name)
                .containsExactly("mix", "bake");
    }

    @Test
    void choosesLowestCostPath() {
        var step1 = Condition.custom("step1");
        var done = Condition.custom("done");
        var state = Beliefs.empty();
        var cheapPath1 = action("cheapA", Set.of(), Set.of(Effect.establishes(step1)), 0.1);
        var cheapPath2 = action("cheapB",
                Set.of(Precondition.requires(step1)),
                Set.of(Effect.establishes(done)), 0.1);
        var expensiveSingle = action("expensive", Set.of(), Set.of(Effect.establishes(done)), 0.9);
        var goal = goal("finish", Precondition.requires(done));

        var plan = planner.plan(state, Set.of(cheapPath1, cheapPath2, expensiveSingle), goal);

        assertThat(plan).isPresent();
        assertThat(plan.get().totalCost(state)).isLessThan(0.9);
        assertThat(plan.get().steps()).extracting(ActionDefinition::name)
                .containsExactly("cheapA", "cheapB");
    }

    @Test
    void unreachableGoal_returnsEmpty() {
        var x = Condition.custom("x");
        var y = Condition.custom("y");
        var state = Beliefs.empty();
        var action = action("doSomething", Set.of(), Set.of(Effect.establishes(x)), 0.5);
        var goal = goal("impossible", Precondition.requires(y));

        var plan = planner.plan(state, Set.of(action), goal);

        assertThat(plan).isEmpty();
    }

    @Test
    void threeStepChain() {
        var hasA = Condition.custom("hasA");
        var hasB = Condition.custom("hasB");
        var hasC = Condition.custom("hasC");
        var state = Beliefs.empty();
        var a1 = action("step1", Set.of(), Set.of(Effect.establishes(hasA)), 0.2);
        var a2 = action("step2",
                Set.of(Precondition.requires(hasA)),
                Set.of(Effect.establishes(hasB)), 0.3);
        var a3 = action("step3",
                Set.of(Precondition.requires(hasB)),
                Set.of(Effect.establishes(hasC)), 0.4);
        var goal = goal("getC", Precondition.requires(hasC));

        var plan = planner.plan(state, Set.of(a1, a2, a3), goal);

        assertThat(plan).isPresent();
        assertThat(plan.get().steps()).extracting(ActionDefinition::name)
                .containsExactly("step1", "step2", "step3");
    }

    @Test
    void preconditionsNotMet_actionNotApplicable() {
        var hasKey = Condition.custom("hasKey");
        var isOpen = Condition.custom("isOpen");
        var state = Beliefs.empty();
        var locked = action("open",
                Set.of(Precondition.requires(hasKey)),
                Set.of(Effect.establishes(isOpen)), 0.5);
        var goal = goal("enter", Precondition.requires(isOpen));

        var plan = planner.plan(state, Set.of(locked), goal);

        assertThat(plan).isEmpty();
    }

    @Test
    void noActions_goalNotSatisfied_returnsEmpty() {
        var done = Condition.custom("done");
        var state = Beliefs.empty();
        var goal = goal("x", Precondition.requires(done));

        var plan = planner.plan(state, Set.of(), goal);

        assertThat(plan).isEmpty();
    }

    private static ActionDefinition action(
            String name,
            Set<Precondition> preconditions,
            Set<Effect> effects,
            double cost) {
        return new ActionDefinition(
                name, name + " description",
                preconditions, effects,
                Set.of(), Set.of(),
                cost, false);
    }

    private static GoalDefinition goal(String name, Precondition... preconditions) {
        return new GoalDefinition(name, name + " description", Set.of(preconditions),
                TypeToken.of(Object.class), 1.0);
    }
}
