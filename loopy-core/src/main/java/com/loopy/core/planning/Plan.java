package com.loopy.core.planning;

import com.loopy.core.action.ActionDefinition;
import com.loopy.core.agent.GoalDefinition;

import java.util.Collections;
import java.util.List;

/**
 * An ordered sequence of actions to achieve a goal.
 *
 * The plan should be reassessed after each action is performed,
 * since the beliefs may have changed. Advancing through the steps 
 * creates a new {@code Plan} via {@link #withoutFirstStep()}.
 *
 * @param steps the ordered list of action definitions to execute
 * @param goal  the goal this plan aims to achieve
 */
public record Plan(List<ActionDefinition> steps, GoalDefinition goal) {

    public Plan {
        steps = Collections.unmodifiableList(List.copyOf(steps));
    }

    /**
     * Check if all steps have been consumed.
     *
     * @return {@code true} if no remaining steps exist
     */
    public boolean isComplete() {
        return steps.isEmpty();
    }

    /**
     * Return the next action to execute.
     *
     * @return the first unconsumed action definition
     * @throws IllegalStateException if the plan is already complete
     */
    public ActionDefinition nextStep() {
        if (isComplete()) {
            throw new IllegalStateException("Plan is already complete");
        }
        return steps.getFirst();
    }

    /**
     * Return a new plan with the first step removed.
     *
     * @return a new plan without the first step
     * @throws IllegalStateException if the plan is already complete
     */
    public Plan withoutFirstStep() {
        if (isComplete()) {
            throw new IllegalStateException("Plan is already complete");
        }
        return new Plan(steps.subList(1, steps.size()), goal);
    }

    /**
     * Compute the total cost of all remaining steps.
     *
     * @param beliefs the current belief state (unused in current cost model)
     * @return the sum of all step costs
     */
    public double totalCost(Beliefs beliefs) {
        return steps.stream().mapToDouble(ActionDefinition::cost).sum();
    }

    /**
     * Compute the net value of this plan: goal value minus total action cost.
     *
     * @param beliefs the current belief state
     * @return the net value (higher is better)
     */
    public double netValue(Beliefs beliefs) {
        return goal.value() - totalCost(beliefs);
    }

    /**
     * Return the number of remaining steps.
     *
     * @return the step count
     */
    public int stepCount() {
        return steps.size();
    }
}
