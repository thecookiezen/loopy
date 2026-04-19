package com.loopy.core.action;

import com.loopy.core.agent.AgentDefinition;
import com.loopy.core.llm.LlmClient;
import com.loopy.core.mailbox.Mailbox;
import com.loopy.core.tool.ToolRegistry;

/**
 * Context passed to every action execution.
 * Provides access to the {@link Mailbox}, {@link LlmClient},
 * {@link ToolRegistry}, and inputs.
 *
 * Actions never share mutable state, they communicate exclusively
 * through the {@link Mailbox}.
 */
public record ActionContext(
        Mailbox mailbox,
        LlmClient llmClient,
        ToolRegistry toolRegistry,
        AgentDefinition agent,
        ActionDefinition currentAction) {

    /**
     * Returns the resolved inputs for this action.
     *
     * All inputs declared via {@link ActionDefinition#inputTypes()} are resolved
     * from the mailbox and validated eagerly. The returned {@link ActionInputs}
     * container provides access without the action needing to know
     * any mapping logic.
     *
     * @return a fully populated {@link ActionInputs} container
     * @throws IllegalStateException if any declared input type has no matching
     *                               message in the mailbox
     */
    public ActionInputs inputs() {
        return ActionInputs.resolve(currentAction, mailbox);
    }

    /**
     * Convenience shorthand for single-input actions.
     *
     * Equivalent to {@code inputs().get(type)}. Useful when an action
     * has a single declared input and the caller knows the type.
     *
     * @param type the expected input class
     * @param <T>  the input type
     * @return the most recent message of the requested type
     * @throws IllegalStateException if no message of the requested type exists in
     *                               the mailbox
     */
    public <T> T input(Class<T> type) {
        return inputs().get(type);
    }
}
