package com.loopy.core.tool;

import com.loopy.core.llm.ToolCallRequest;

/**
 * Executes a single tool call request against a tool registry.
 *
 * Implementations handle the full lifecycle of a tool invocation:
 * resolving the tool by name, executing it, handling errors, and
 * producing a {@link ToolCallResult}.
 *
 * @see DefaultToolCallExecutor
 */
@FunctionalInterface
public interface ToolCallExecutor {

    /**
     * Execute a tool call request.
     *
     * @param request the tool call request from the LLM
     * @param registry the registry of available tools
     * @return the result of the tool execution
     */
    ToolCallResult execute(ToolCallRequest request, ToolRegistry registry);
}
