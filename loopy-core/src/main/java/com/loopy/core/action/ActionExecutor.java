package com.loopy.core.action;

/**
 * Executes an action given the current {@link ActionContext}.
 *
 * Implementations encapsulate the side-effecting logic of an action (e.g. calling
 * an LLM, invoking a tool, processing data). The context provides access to the
 * {@link Mailbox}, {@link LlmClient},
 * and {@link ToolRegistry}. Results are returned as an
 * {@link ActionResult} and posted to the mailbox by the runtime.
 *
 * @see ActionContext
 * @see ActionResult
 * @see ActionBinding
 */
@FunctionalInterface
public interface ActionExecutor {
    /**
     * Execute the action and return the result.
     *
     * @param context context providing access to mailbox, LLM client, and tools
     * @return the result of the action execution
     */
    ActionResult execute(ActionContext context);
}
