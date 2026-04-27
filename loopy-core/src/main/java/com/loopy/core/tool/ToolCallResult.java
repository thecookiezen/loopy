package com.loopy.core.tool;

import com.loopy.core.llm.ChatMessage;

import java.time.Duration;

/**
 * Result of executing a single tool call requested by the LLM.
 *
 * Captures the tool call ID, the tool name, arguments, result content,
 * and execution duration. Used for both feeding results back to the LLM
 * and for execution tracing.
 *
 * @param toolCallId    the unique identifier of the tool call, correlating
 *                      with the original {@link com.loopy.core.llm.ToolCallRequest}
 * @param toolName      the name of the tool that was invoked
 * @param argumentsJson the JSON-encoded arguments passed to the tool
 * @param resultContent the result returned by the tool (or error message)
 * @param duration      the wall-clock duration of the tool execution
 */
public record ToolCallResult(
        String toolCallId,
        String toolName,
        String argumentsJson,
        String resultContent,
        Duration duration) {

    /**
     * Derive a {@link ChatMessage.ToolResult} suitable for appending to the
     * conversation and sending back to the LLM.
     *
     * @return a tool result message correlated with the original tool call ID
     */
    public ChatMessage.ToolResult toToolResultMessage() {
        return new ChatMessage.ToolResult(toolCallId, resultContent);
    }
}
