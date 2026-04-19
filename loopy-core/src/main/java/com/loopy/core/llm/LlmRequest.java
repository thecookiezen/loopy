package com.loopy.core.llm;

import com.loopy.core.TypeToken;
import com.loopy.core.tool.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Request to an LLM, containing the conversation, available tools,
 * and generation parameters.
 *
 * @param model         the model identifier (e.g. "gpt-5.4-mini")
 * @param messages      the conversation history
 * @param tools         the tools available to the model
 * @param responseType  the expected response type token
 * @param temperature   the sampling temperature (0.0 &ndash; 2.0)
 * @param maxTokens     optional maximum number of completion tokens
 */
public record LlmRequest(
        String model,
        List<ChatMessage> messages,
        List<Tool> tools,
        TypeToken<?> responseType,
        double temperature,
        Optional<Integer> maxTokens) {
    public LlmRequest {
        messages = List.copyOf(messages);
        tools = List.copyOf(tools);
    }

    /**
     * Create a simple request with a single user message, no tools,
     * default temperature (0.7), and no token limit.
     *
     * @param model       the model identifier
     * @param userPrompt  the user's prompt text
     * @return a minimal LLM request
     */
    public static LlmRequest simple(String model, String userPrompt) {
        return new LlmRequest(
                model,
                List.of(new ChatMessage.User(userPrompt)),
                List.of(),
                TypeToken.of(String.class),
                0.7,
                Optional.empty());
    }

    /**
     * Return a copy with a system prompt prepended to the messages.
     *
     * @param system the system prompt text
     * @return a new request with the system prompt added
     */
    public LlmRequest withSystemPrompt(String system) {
        var updated = new ArrayList<ChatMessage>();
        updated.add(new ChatMessage.System(system));
        updated.addAll(messages);
        return new LlmRequest(model, updated, tools, responseType, temperature, maxTokens);
    }

    /**
     * Return a copy with replaced messages.
     *
     * @param newMessages the new message list
     * @return a new request with the updated messages
     */
    public LlmRequest withMessages(List<ChatMessage> newMessages) {
        return new LlmRequest(model, newMessages, tools, responseType, temperature, maxTokens);
    }

    /**
     * Return a copy with replaced tools.
     *
     * @param newTools the new tool list
     * @return a new request with the updated tools
     */
    public LlmRequest withTools(List<Tool> newTools) {
        return new LlmRequest(model, messages, newTools, responseType, temperature, maxTokens);
    }

    /**
     * Return a copy with a different response format type.
     *
     * @param responseType the new response type token
     * @return a new request with the updated response format
     */
    public LlmRequest withResponseFormat(TypeToken<?> responseType) {
        return new LlmRequest(model, messages, tools, responseType, temperature, maxTokens);
    }
}
