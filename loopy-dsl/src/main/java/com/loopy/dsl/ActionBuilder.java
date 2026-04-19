package com.loopy.dsl;

import com.loopy.core.TypeToken;
import com.loopy.core.action.ActionBinding;
import com.loopy.core.action.ActionDefinition;
import com.loopy.core.action.ActionExecutor;
import com.loopy.core.condition.Condition;
import com.loopy.core.condition.Effect;
import com.loopy.core.condition.ExpressionEvaluator;
import com.loopy.core.condition.Precondition;

import java.util.*;
import java.util.function.Predicate;

public final class ActionBuilder {

    private final AgentBuilder parent;
    private final String name;
    private String description = "";
    private final Set<Precondition> preconditions = new LinkedHashSet<>();
    private final Set<Effect> effects = new LinkedHashSet<>();
    private final Set<TypeToken<?>> inputTypes = new LinkedHashSet<>();
    private final Set<TypeToken<?>> outputTypes = new LinkedHashSet<>();
    private double cost = 0.5;
    private boolean canRerun = false;
    private ActionExecutor executor;

    ActionBuilder(AgentBuilder parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    /**
     * Set the action description.
     *
     * @param description human-readable description
     * @return this builder
     */
    public ActionBuilder describedAs(String description) {
        this.description = description;
        return this;
    }

    /**
     * Declare input types for this action.
     *
     * @param types the required input types
     * @return this builder
     */
    public ActionBuilder input(Class<?>... types) {
        for (var type : types) {
            inputTypes.add(TypeToken.of(type));
        }
        return this;
    }

    /**
     * Declare output types for this action.
     *
     * @param types the output types this action produces
     * @return this builder
     */
    public ActionBuilder output(Class<?>... types) {
        for (var type : types) {
            outputTypes.add(TypeToken.of(type));
        }
        return this;
    }

    /**
     * Add a requirement that the given condition must be satisfied
     * for this action to be applicable.
     *
     * @param condition the required condition
     * @return this builder
     */
    public ActionBuilder requires(Condition condition) {
        preconditions.add(Precondition.requires(condition));
        return this;
    }

    /**
     * Add a requirement that the given condition must NOT be satisfied
     * for this action to be applicable.
     *
     * @param condition the forbidden condition
     * @return this builder
     */
    public ActionBuilder forbids(Condition condition) {
        preconditions.add(Precondition.forbids(condition));
        return this;
    }

    /**
     * Declare that this action establishes (makes true) the given condition.
     *
     * @param condition the condition to establish
     * @return this builder
     */
    public ActionBuilder establishes(Condition condition) {
        effects.add(Effect.establishes(condition));
        return this;
    }

    /**
     * Declare that this action revokes (makes false) the given condition.
     *
     * @param condition the condition to revoke
     * @return this builder
     */
    public ActionBuilder revokes(Condition condition) {
        effects.add(Effect.revokes(condition));
        return this;
    }

    /**
     * Register a predicate as both a precondition on this action
     * and a condition binding on the agent.
     *
     * Equivalent to calling {@code requires(Condition.custom(name))}
     * plus {@code agent.condition(name, type, test)}.
     *
     * @param name the condition name
     * @param type the mailbox message type to test
     * @param test the predicate to evaluate
     * @param <T>  the message type
     * @return this builder
     */
    public <T> ActionBuilder requiresPredicate(
            String name, Class<T> type, Predicate<T> test) {
        preconditions.add(Precondition.requires(Condition.custom(name)));
        parent.condition(name, type, test);
        return this;
    }

    /**
     * Register an expression-based condition as both a precondition on this action
     * and a condition binding on the agent.
     *
     * Equivalent to calling {@code requires(Condition.custom(name))}
     * plus {@code agent.condition(name, type, expression, engine)}.
     *
     * @param name       the condition name
     * @param type       the mailbox message type to bind as context
     * @param expression the expression string
     * @param engine     the expression evaluator engine
     * @return this builder
     */
    public ActionBuilder requiresExpression(
            String name, Class<?> type, String expression, ExpressionEvaluator engine) {
        preconditions.add(Precondition.requires(Condition.custom(name)));
        parent.condition(name, type, expression, engine);
        return this;
    }

    /**
     * Set the action cost (0.0 &ndash; 1.0). Lower-cost actions are preferred
     * by the planner. Defaults to 0.5.
     *
     * @param cost the action cost
     * @return this builder
     */
    public ActionBuilder cost(double cost) {
        this.cost = cost;
        return this;
    }

    /**
     * Set whether this action can be rerun if it fails.
     *
     * @param canRerun true if the action is safe to rerun
     * @return this builder
     */
    public ActionBuilder canRerun(boolean canRerun) {
        this.canRerun = canRerun;
        return this;
    }

    /**
     * Set the executable behavior for this action.
     *
     * @param executor the action executor
     * @return this builder
     * @see com.loopy.core.action.ActionExecutor
     */
    public ActionBuilder executor(ActionExecutor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Add this action to the parent agent builder.
     *
     * @return the parent agent builder for further chaining
     * @throws IllegalStateException if no executor has been set
     */
    public AgentBuilder add() {
        if (executor == null) {
            throw new IllegalStateException("Action '" + name + "' must have an executor");
        }
        var definition = new ActionDefinition(
                name, description, preconditions, effects,
                inputTypes, outputTypes, cost, canRerun);
        return parent.addAction(new ActionBinding(definition, executor));
    }
}
