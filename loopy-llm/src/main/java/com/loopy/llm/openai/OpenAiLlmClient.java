package com.loopy.llm.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopy.core.TokenUsage;
import com.loopy.core.llm.*;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * LLM client implementation using the official OpenAI Java SDK.
 */
public final class OpenAiLlmClient implements LlmClient {

        private static final Logger LOG = LoggerFactory.getLogger(OpenAiLlmClient.class);

        private final OpenAIClient client;

        public OpenAiLlmClient(OpenAiConfig config) {
                var builder = OpenAIOkHttpClient.builder()
                                .apiKey(config.apiKey())
                                .timeout(config.timeout());
                if (!config.baseUrl().equals(OpenAiConfig.DEFAULT_BASE_URL)) {
                        builder.baseUrl(config.baseUrl());
                }
                this.client = builder.build();
        }

        public OpenAiLlmClient(OpenAIClient client) {
                this.client = client;
        }

        @Override
        public LlmResponse chat(LlmRequest request) {
                var params = buildParams(request);

                LOG.debug("Sending chat request to model '{}' with {} messages",
                                request.model(), request.messages().size());

                var chatCompletion = client.chat().completions().create(params);
                var choice = chatCompletion.choices().getFirst();

                // Extract tool calls from response
                var toolCalls = choice.message().toolCalls()
                                .stream()
                                .flatMap(Collection::stream)
                                .map(tc -> new ToolCallRequest(
                                                tc.asFunction().id(),
                                                tc.asFunction().function().name(),
                                                tc.asFunction().function().arguments()))
                                .toList();

                var assistantMessage = new ChatMessage.Assistant(
                                choice.message().content().orElse(""),
                                toolCalls);

                var usage = chatCompletion.usage()
                                .map(u -> new TokenUsage(
                                                (int) u.promptTokens(),
                                                (int) u.completionTokens()))
                                .orElse(TokenUsage.zero());

                return new LlmResponse(
                                assistantMessage,
                                usage,
                                chatCompletion.model(),
                                choice.finishReason().toString());
        }

        @Override
        public <T> LlmObjectResult<T> createObject(LlmRequest request, Class<T> outputType) {
                LOG.debug("Sending structured output request for type '{}'", outputType.getSimpleName());

                var paramsBuilder = ChatCompletionCreateParams.builder()
                                .model(request.model())
                                .temperature(request.temperature());

                request.maxTokens().ifPresent(paramsBuilder::maxCompletionTokens);
                addMessages(paramsBuilder, request.messages());

                var structuredParams = paramsBuilder
                                .responseFormat(outputType)
                                .build();

                var result = client.chat().completions().create(structuredParams);
                var choice = result.choices().getFirst();

                var usage = result.usage()
                                .map(u -> new TokenUsage(
                                                (int) u.promptTokens(),
                                                (int) u.completionTokens()))
                                .orElse(TokenUsage.zero());

                T value = choice.message().content()
                                .orElseThrow(() -> new RuntimeException(
                                                "No structured output content in response for type "
                                                                + outputType.getSimpleName()));

                return new LlmObjectResult<>(value, usage);
        }

        private ChatCompletionCreateParams buildParams(LlmRequest request) {
                var builder = ChatCompletionCreateParams.builder()
                                .model(request.model())
                                .temperature(request.temperature());

                request.maxTokens().ifPresent(builder::maxCompletionTokens);
                addMessages(builder, request.messages());
                addTools(builder, request);

                return builder.build();
        }

        private void addMessages(ChatCompletionCreateParams.Builder builder, List<ChatMessage> messages) {
                for (var msg : messages) {
                        switch (msg) {
                                case ChatMessage.System s ->
                                        builder.addSystemMessage(s.content());
                                case ChatMessage.User u ->
                                        builder.addUserMessage(u.content());
                                case ChatMessage.Assistant a -> {
                                        if (a.toolCalls() != null && !a.toolCalls().isEmpty()) {
                                                // Assistant message with tool calls - re-add using addMessage
                                                builder.addAssistantMessage(a.content());
                                        } else {
                                                builder.addAssistantMessage(a.content());
                                        }
                                }
                                case ChatMessage.ToolResult t ->
                                        builder.addMessage(ChatCompletionToolMessageParam.builder()
                                                        .toolCallId(t.toolCallId())
                                                        .content(t.content())
                                                        .build());
                        }
                }
        }

        private void addTools(ChatCompletionCreateParams.Builder builder, LlmRequest request) {
                for (var tool : request.tools()) {
                        // Build tool definition from JSON schema string
                        var schemaMap = parseJsonToMap(tool.parametersSchemaJson());
                        var paramsBuilder = FunctionParameters.builder();
                        schemaMap.forEach((key, value) -> paramsBuilder.putAdditionalProperty(key,
                                        JsonValue.from(value)));

                        builder.addTool(ChatCompletionFunctionTool.builder()
                                        .function(FunctionDefinition.builder()
                                                        .name(tool.name())
                                                        .description(tool.description())
                                                        .parameters(paramsBuilder.build())
                                                        .build())
                                        .build());
                }
        }

        @SuppressWarnings("unchecked")
        private static Map<String, Object> parseJsonToMap(String json) {
                try {
                        return new ObjectMapper().readValue(json, Map.class);
                } catch (Exception e) {
                        return Map.of("type", "object", "properties", Map.of());
                }
        }
}
