package com.loopy.dsl;

import com.loopy.core.condition.Condition;
import com.loopy.core.condition.Precondition;
import com.loopy.core.TypeToken;
import com.loopy.core.agent.GoalDefinition;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Fluent builder for defining a goal within an {@link AgentBuilder}.
 *
 * A goal specifies what the agent wants to achieve via a set of
 * preconditions that must hold, an output type, and a value (0.0–1.0)
 * indicating the goal's importance.
 */
public final class GoalBuilder {

    private final AgentBuilder parent;
    private final String name;
    private String description = "";
    private final Set<Precondition> preconditions = new LinkedHashSet<>();
    private TypeToken<?> outputType = TypeToken.of(Object.class);
    private double value = 1.0;

    GoalBuilder(AgentBuilder parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    /**
     * Set the goal description.
     *
     * @param description human-readable description
     * @return this builder
     */
    public GoalBuilder describedAs(String description) {
        this.description = description;
        return this;
    }

    /**
     * Declare that this goal is satisfied when the given type is present
     * in the mailbox.
     *
     * @param type the type whose presence in the mailbox satisfies this goal
     * @return this builder
     */
    public GoalBuilder satisfiedBy(Class<?> type) {
        this.outputType = TypeToken.of(type);
        return this;
    }

    /**
     * Add a requirement that the given condition must hold for the goal
     * to be considered achieved.
     *
     * @param condition the required condition
     * @return this builder
     */
    public GoalBuilder requires(Condition condition) {
        preconditions.add(Precondition.requires(condition));
        return this;
    }

    /**
     * Add a requirement that the given condition must NOT hold for the goal
     * to be considered achieved.
     *
     * @param condition the forbidden condition
     * @return this builder
     */
    public GoalBuilder forbids(Condition condition) {
        preconditions.add(Precondition.forbids(condition));
        return this;
    }

    /**
     * Set the goal value (0.0 &ndash; 1.0). Higher values make the planner
     * prefer this goal when choosing between alternatives. Defaults to 1.0.
     *
     * @param value the goal value
     * @return this builder
     */
    public GoalBuilder value(double value) {
        this.value = value;
        return this;
    }

    /**
     * Add this goal to the parent agent builder.
     *
     * @return the parent agent builder for further chaining
     */
    public AgentBuilder add() {
        var goal = new GoalDefinition(name, description, preconditions, outputType, value);
        return parent.addGoal(goal);
    }
}
