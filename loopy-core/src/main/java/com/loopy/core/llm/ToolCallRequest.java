package com.loopy.core.llm;

/**
 * Represents a single tool call requested by the LLM in a chat completion response.
 *
 * @param id            the unique identifier for this tool call, used to correlate
 *                      with the {@link ChatMessage.ToolResult}
 * @param functionName  the name of the tool function to invoke
 * @param argumentsJson the JSON-encoded arguments for the function call
 */
public record ToolCallRequest(String id, String functionName, String argumentsJson) {
}
