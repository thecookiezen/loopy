package com.loopy.dsl;

import com.loopy.core.action.ActionBinding;
import com.loopy.core.agent.AgentDefinition;
import com.loopy.core.agent.GoalDefinition;
import com.loopy.core.agent.SupervisionStrategy;
import com.loopy.core.condition.Condition;
import com.loopy.core.TypeToken;
import com.loopy.core.condition.ConditionBinding;
import com.loopy.core.condition.ConditionEvaluator;
import com.loopy.core.condition.ExpressionEvaluator;

import java.util.function.Predicate;
import java.util.*;

/**
 * Fluent builder for constructing {@link AgentDefinition} instances.
 *
 * Provides a readable DSL for defining agents with actions, goals,
 * conditions, and supervision strategies:
 *
 * {@code
 * var agent = AgentBuilder.named("my-agent")
 *         .describedAs("Does things")
 *         .action("step1")
 *             .input(Input.class).output(Output.class)
 *             .executor(ctx -> ActionResult.Success.of("result"))
 *             .add()
 *         .goal("finish")
 *             .satisfiedBy(Output.class)
 *             .add()
 *         .build();
 * }
 */
public final class AgentBuilder {

    private final String name;
    private String description = "";
    private final List<ActionBinding> actions = new ArrayList<>();
    private final Set<GoalDefinition> goals = new LinkedHashSet<>();
    private final Set<ConditionBinding> conditions = new LinkedHashSet<>();
    private SupervisionStrategy defaultSupervision = new SupervisionStrategy.Replan();

    private AgentBuilder(String name) {
        this.name = name;
    }

    /**
     * Start building an agent with the given name.
     *
     * @param name the unique agent name
     * @return a new builder
     */
    public static AgentBuilder named(String name) {
        return new AgentBuilder(name);
    }

    /**
     * Set the agent description.
     *
     * @param description human-readable description of the agent's purpose
     * @return this builder
     */
    public AgentBuilder describedAs(String description) {
        this.description = description;
        return this;
    }

    /**
     * Set the default supervision strategy for action failures.
     *
     * @param strategy the supervision strategy
     * @return this builder
     * @see SupervisionStrategy
     */
    public AgentBuilder supervision(SupervisionStrategy strategy) {
        this.defaultSupervision = strategy;
        return this;
    }

    /**
     * Start defining an action with the given name.
     * Returns an {@link ActionBuilder} that chains back to this builder
     * via {@link ActionBuilder#add()}.
     *
     * @param name the action name
     * @return an action builder scoped to this agent
     */
    public ActionBuilder action(String name) {
        return new ActionBuilder(this, name);
    }

    /**
     * Start defining a goal with the given name.
     * Returns a {@link GoalBuilder} that chains back to this builder
     * via {@link GoalBuilder#add()}.
     *
     * @param name the goal name
     * @return a goal builder scoped to this agent
     */
    public GoalBuilder goal(String name) {
        return new GoalBuilder(this, name);
    }

    /**
     * Register a custom condition binding with an explicit evaluator.
     *
     * @param condition the custom condition
     * @param evaluator the evaluator for this condition
     * @return this builder
     */
    public AgentBuilder condition(Condition.Custom condition, ConditionEvaluator evaluator) {
        conditions.add(ConditionBinding.custom(condition, evaluator));
        return this;
    }

    /**
     * Register a custom condition binding by name.
     *
     * @param name      the condition name
     * @param evaluator the evaluator for this condition
     * @return this builder
     */
    public AgentBuilder condition(String name, ConditionEvaluator evaluator) {
        conditions.add(ConditionBinding.custom(name, evaluator));
        return this;
    }

    /**
     * Register a type-predicate condition binding.
     * The condition is satisfied when the latest mailbox message of
     * {@code targetType} passes the given predicate.
     *
     * @param name       the condition name
     * @param targetType the mailbox message type to test
     * @param test       the predicate to evaluate against the latest message
     * @param <T>        the message type
     * @return this builder
     */
    public <T> AgentBuilder condition(
            String name, Class<T> targetType, Predicate<T> test) {
        conditions.add(ConditionBinding.typePredicate(name, TypeToken.of(targetType), test));
        return this;
    }

    /**
     * Register an expression-based condition binding.
     *
     * @param name       the condition name
     * @param targetType the mailbox message type to bind as context
     * @param expression the expression string
     * @param engine     the expression evaluator engine
     * @return this builder
     */
    public AgentBuilder condition(
            String name, Class<?> targetType, String expression, ExpressionEvaluator engine) {
        conditions.add(ConditionBinding.expression(name, targetType, expression, engine));
        return this;
    }

    AgentBuilder addAction(ActionBinding binding) {
        actions.add(binding);
        return this;
    }

    AgentBuilder addGoal(GoalDefinition goal) {
        goals.add(goal);
        return this;
    }

    /**
     * Build the {@link AgentDefinition}.
     *
     * @return the constructed agent definition
     * @throws IllegalStateException if no actions or no goals are defined
     */
    public AgentDefinition build() {
        if (actions.isEmpty()) {
            throw new IllegalStateException("Agent must have at least one action");
        }
        if (goals.isEmpty()) {
            throw new IllegalStateException("Agent must have at least one goal");
        }
        return new AgentDefinition(name, description, actions, goals, conditions, defaultSupervision);
    }
}
