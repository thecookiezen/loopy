package com.loopy.core.planning;

import com.loopy.core.action.ActionDefinition;
import com.loopy.core.agent.GoalDefinition;
import com.loopy.core.condition.Effect;
import com.loopy.core.condition.Precondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Goal-Oriented Action Planning using A* search.
 *
 * Finds the lowest-cost sequence of actions that transforms the current world
 * beliefs into one satisfying the goal's preconditions. The algorithm:
 * 1. Start with the initial beliefs
 * 2. Maintain a priority queue ordered by f = g + h (estimated total
 * cost)
 * 3. g = accumulated action cost from start
 * 4. h = heuristic (count of unsatisfied goal conditions - admissible, never
 * overestimates)
 * 5. Expand the lowest-f node: try each applicable action, compute the
 * resulting state
 * 6. Track visited states to avoid cycles
 * 7. Stop when a state satisfying all goal conditions is found
 */
public final class GoapPlanner implements Planner {

    private static final Logger LOG = LoggerFactory.getLogger(GoapPlanner.class);
    private static final int MAX_ITERATIONS = 1_000;

    @Override
    public Optional<Plan> plan(Beliefs current, Set<ActionDefinition> actions, GoalDefinition goal) {
        if (current.satisfies(goal.preconditions())) {
            LOG.debug("Goal '{}' already satisfied in current state", goal.name());
            return Optional.of(new Plan(List.of(), goal));
        }

        if (!isReachable(current, actions, goal)) {
            LOG.debug("Goal '{}' is unreachable - no action produces required effects", goal.name());
            return Optional.empty();
        }

        double minActionCost = actions.stream()
                .mapToDouble(ActionDefinition::cost)
                .filter(c -> c > 0.0)
                .min()
                .orElse(0.0);

        var openQueue = new PriorityQueue<SearchNode>();
        var bestCosts = new HashMap<Beliefs, Double>();
        var cameFrom = new HashMap<Beliefs, ActionDefinition>();
        var parentState = new HashMap<Beliefs, Beliefs>();

        bestCosts.put(current, 0.0);
        openQueue.add(new SearchNode(current, 0.0, heuristic(current, goal, minActionCost)));

        int iterations = 0;

        while (!openQueue.isEmpty() && iterations < MAX_ITERATIONS) {
            iterations++;
            var node = openQueue.poll();

            if (node.state.satisfies(goal.preconditions())) {
                var path = reconstructPath(node.state, cameFrom, parentState, current);
                LOG.debug("Plan found for goal '{}' in {} iterations: {} actions",
                        goal.name(), iterations, path.size());
                return Optional.of(new Plan(path, goal));
            }

            // Skip if we already found a cheaper path to this state
            if (node.gScore > bestCosts.getOrDefault(node.state, Double.MAX_VALUE)) {
                continue;
            }

            for (var action : actions) {
                if (!node.state.satisfies(action.preconditions())) {
                    continue;
                }

                var nextState = node.state.applyEffects(action.effects());

                if (nextState.equals(node.state)) {
                    continue;
                }

                double tentativeCost = node.gScore + action.cost();

                if (tentativeCost < bestCosts.getOrDefault(nextState, Double.MAX_VALUE)) {
                    bestCosts.put(nextState, tentativeCost);
                    cameFrom.put(nextState, action);
                    parentState.put(nextState, node.state);
                    openQueue.add(new SearchNode(nextState, tentativeCost, heuristic(nextState, goal, minActionCost)));
                }
            }
        }

        LOG.debug("No plan found for goal '{}' after {} iterations", goal.name(), iterations);
        return Optional.empty();
    }

    /**
     * Fast reachability check: verify that for every unsatisfied goal precondition,
     * at least one action exists that can produce the needed effect.
     */
    private boolean isReachable(Beliefs current, Set<ActionDefinition> actions, GoalDefinition goal) {
        for (var precondition : goal.preconditions()) {
            if (current.isSatisfied(precondition)) {
                continue;
            }
            boolean canBeProduced = actions.stream()
                    .anyMatch(a -> wouldSatisfy(a, precondition));
            if (!canBeProduced) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if an action's effects would satisfy a given precondition.
     */
    private boolean wouldSatisfy(ActionDefinition action, Precondition precondition) {
        return switch (precondition) {
            case Precondition.Requires r ->
                action.effects().stream()
                        .anyMatch(e -> e instanceof Effect.Establishes est && est.condition().equals(r.condition()));
            case Precondition.Forbids f ->
                action.effects().stream()
                        .anyMatch(e -> e instanceof Effect.Revokes rev && rev.condition().equals(f.condition()));
        };
    }

    /**
     * Count of unsatisfied goal conditions × minimum action cost.
     * Don't overestimates because each unsatisfied condition requires at least one action
     * with cost >= minActionCost to satisfy.
     */
    private double heuristic(Beliefs state, GoalDefinition goal, double minActionCost) {
        long unsatisfied = goal.preconditions().stream()
                .filter(p -> !state.isSatisfied(p))
                .count();
        return unsatisfied * minActionCost;
    }

    private List<ActionDefinition> reconstructPath(Beliefs goalState, Map<Beliefs, ActionDefinition> cameFrom,
            Map<Beliefs, Beliefs> parentState, Beliefs startState) {
        var actions = new ArrayList<ActionDefinition>();
        var current = goalState;
        while (!current.equals(startState) && cameFrom.containsKey(current)) {
            actions.add(cameFrom.get(current));
            current = parentState.get(current);
        }
        Collections.reverse(actions);
        return actions;
    }

    /**
     * A node in the A* search. Ordered by f-score (estimated total cost).
     */
    private record SearchNode(Beliefs state, double gScore, double hScore) implements Comparable<SearchNode> {
        double fScore() {
            return gScore + hScore;
        }

        @Override
        public int compareTo(SearchNode other) {
            return Double.compare(this.fScore(), other.fScore());
        }
    }
}
