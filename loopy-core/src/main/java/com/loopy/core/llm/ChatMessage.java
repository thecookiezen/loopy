package com.loopy.core.llm;

import java.util.List;

/**
 * Represents a single message in an LLM conversation.
 */
public sealed interface ChatMessage {

    /**
     * System message providing instructions or context to the model.
     *
     * @param content the system prompt text
     */
    record System(String content) implements ChatMessage {
    }

    /**
     * User message containing the human's input.
     *
     * @param content the user's message text
     */
    record User(String content) implements ChatMessage {
    }

    /**
     * Assistant message containing the model's response, optionally with tool calls.
     *
     * @param content   the text content of the response (may be empty when tool calls are present)
     * @param toolCalls the tool calls requested by the model, or an empty list
     */
    record Assistant(String content, List<ToolCallRequest> toolCalls) implements ChatMessage {
        /**
         * Construct an assistant message with no tool calls.
         *
         * @param content the text content of the response
         */
        public Assistant(String content) {
            this(content, List.of());
        }
    }

    /**
     * Result of executing a tool call, to be sent back to the model.
     *
     * @param toolCallId the ID of the tool call this result corresponds to
     * @param content    the result content (typically JSON-encoded)
     */
    record ToolResult(String toolCallId, String content) implements ChatMessage {
    }
}
