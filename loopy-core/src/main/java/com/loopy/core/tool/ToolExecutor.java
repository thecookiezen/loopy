package com.loopy.core.tool;

/**
 * Executes a tool call with JSON-encoded arguments.
 */
@FunctionalInterface
public interface ToolExecutor {
    String execute(String argumentsJson);
}
