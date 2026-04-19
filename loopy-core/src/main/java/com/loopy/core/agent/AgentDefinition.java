package com.loopy.core.agent;

import com.loopy.core.action.ActionBinding;
import com.loopy.core.condition.ConditionBinding;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Blueprint defining an agent's capabilities and objectives.
 *
 * It is the static specification of an agent: its name,
 * description, the {@link ActionBinding actions} it can perform, the
 * {@link GoalDefinition goals} it pursues, the {@link ConditionBinding conditions}
 * it evaluates, and the default {@link SupervisionStrategy} for handling failures.
 *
 * @param name               unique name identifying this agent type
 * @param description        human-readable description of the agent's purpose
 * @param actions            the set of action bindings (metadata + executable behavior)
 * @param goals              the set of goals this agent can pursue
 * @param conditions         condition bindings for belief derivation
 * @param defaultSupervision the default strategy when an action fails
 */
public record AgentDefinition(
        String name,
        String description,
        List<ActionBinding> actions,
        Set<GoalDefinition> goals,
        Set<ConditionBinding> conditions,
        SupervisionStrategy defaultSupervision) {
    public AgentDefinition {
        actions = Collections.unmodifiableList(List.copyOf(actions));
        goals = Collections.unmodifiableSet(Set.copyOf(goals));
        conditions = Collections.unmodifiableSet(Set.copyOf(conditions));
    }

    /**
     * Convenience constructor that defaults to {@link SupervisionStrategy.Replan}
     * as the supervision strategy.
     *
     * @param name        unique name identifying this agent type
     * @param description human-readable description of the agent's purpose
     * @param actions     the set of action bindings
     * @param goals       the set of goals this agent can pursue
     * @param conditions  condition bindings for belief derivation
     */
    public AgentDefinition(
            String name,
            String description,
            List<ActionBinding> actions,
            Set<GoalDefinition> goals,
            Set<ConditionBinding> conditions) {
        this(name, description, actions, goals, conditions, new SupervisionStrategy.Replan());
    }
}
