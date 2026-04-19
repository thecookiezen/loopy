package com.loopy.core.agent;

import com.loopy.core.planning.Plan;

/**
 * Represents the current behavioral state of an agent during execution.
 *
 * The agent transitions between these states as it observes the beliefs, plans,
 * and executes actions.
 */
public sealed interface AgentBehavior {
    record Executing(Plan currentPlan, int stepIndex) implements AgentBehavior {
    }

    record Waiting(AgentId agentId, String description) implements AgentBehavior {
    }

    record Completed(Object result) implements AgentBehavior {
    }

    record Stuck(String reason) implements AgentBehavior {
    }

    record Failed(Exception cause) implements AgentBehavior {
    }
}
