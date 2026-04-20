package com.loopy.core.planning;

import com.loopy.core.agent.AgentDefinition;
import com.loopy.core.mailbox.Mailbox;

/**
 * Derives the agent's beliefs from the current mailbox state.
 *
 * Beliefs are a derived view of the mailbox, a projection of the message log
 * into the boolean condition space used by the GOAP planner. This keeps the
 * Mailbox a pure message store and confines planning-layer concerns to this
 * interface.
 *
 */
@FunctionalInterface
public interface BeliefDeriver {

    /**
     * Derive beliefs from the current mailbox and agent definition.
     *
     * @param mailbox the current mailbox state
     * @param agent   the agent definition (used to discover types and condition
     *                evaluators from actions)
     * @return the derived beliefs
     */
    Beliefs derive(Mailbox mailbox, AgentDefinition agent);
}
