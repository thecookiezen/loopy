package com.loopy.core.planning;

import com.loopy.core.action.ActionDefinition;
import com.loopy.core.agent.GoalDefinition;

import java.util.Optional;
import java.util.Set;

/**
 * Interface for planning strategies.
 * Given the current beliefs, available actions, and a goal,
 * produces an optional plan (ordered list of actions to reach the goal).
 *
 * The default implementation is {@link GoapPlanner} which uses A* search.
 * Custom planners can be provided for specialized planning strategies.
 */
@FunctionalInterface
public interface Planner {

    /**
     * Find a plan from the current beliefs to the given goal.
     *
     * @param current the current beliefs
     * @param actions the available actions
     * @param goal    the goal to achieve
     * @return an optional plan, empty if no plan can be found
     */
    Optional<Plan> plan(Beliefs current, Set<ActionDefinition> actions, GoalDefinition goal);

    /**
     * Find and return the best plan to any of the given goals.
     * Plans are ranked by net value (goal value + action values - costs).
     */
    default Optional<Plan> bestPlanToAnyGoal(
            Beliefs current,
            Set<ActionDefinition> actions,
            Set<GoalDefinition> goals) {
        return goals.stream()
                .map(goal -> plan(current, actions, goal))
                .flatMap(Optional::stream)
                .max((a, b) -> Double.compare(a.netValue(current), b.netValue(current)));
    }
}
