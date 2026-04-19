package com.loopy.core.llm;

import com.loopy.core.TokenUsage;

/**
 * Response from an LLM chat completion.
 *
 * @param message      the assistant's response message, which may contain
 *                     text content and/or {@link ToolCallRequest tool calls}
 * @param usage        token usage statistics for this request
 * @param model        the actual model used (may differ from the requested model)
 * @param finishReason the reason the model stopped generating (e.g. "stop", "tool_calls")
 */
public record LlmResponse(
        ChatMessage.Assistant message,
        TokenUsage usage,
        String model,
        String finishReason) {

    /**
     * Check if the response contains tool call requests.
     *
     * @return {@code true} if the model requested one or more tool calls
     */
    public boolean hasToolCalls() {
        return message.toolCalls() != null && !message.toolCalls().isEmpty();
    }
}
