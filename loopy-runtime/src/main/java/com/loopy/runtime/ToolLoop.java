package com.loopy.runtime;

import com.loopy.core.TokenUsage;
import com.loopy.core.tool.ToolRegistry;
import com.loopy.core.llm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Stream;

/**
 * The tool-calling loop.
 */
public final class ToolLoop {

    private static final Logger LOG = LoggerFactory.getLogger(ToolLoop.class);

    /**
     * Snapshot of one iteration of the tool loop.
     */
    private record LoopState(
            List<ChatMessage> messages,
            List<ToolCallRecord> toolCalls,
            TokenUsage usage,
            int iteration,
            LlmResponse lastResponse,
            boolean terminal,
            String interruptReason) {
        static LoopState initial(List<ChatMessage> messages) {
            return new LoopState(messages, List.of(), TokenUsage.zero(), 0, null, false, null);
        }

        LoopState withTerminal(LlmResponse response, TokenUsage newUsage) {
            return new LoopState(messages, toolCalls, newUsage, iteration + 1, response, true, null);
        }

        LoopState withInterrupt(String reason) {
            return new LoopState(messages, toolCalls, usage, iteration, lastResponse, true, reason);
        }
    }

    /**
     * Paired result of executing tool calls - the message results to append
     * to the conversation, plus the records for tracing.
     */
    private record ToolExecutionResult(List<ChatMessage.ToolResult> messages, List<ToolCallRecord> records) {
    }

    /**
     * Execute the tool loop:
     * 1. Send request to LLM
     * 2. If response has tool calls: execute them
     * 3. Append tool results to conversation
     * 4. Produce new state
     * 5. Repeat until LLM returns content without tool calls, or max iterations
     */
    public static ToolLoopResult execute(
            LlmClient client,
            LlmRequest initialRequest,
            ToolRegistry tools,
            int maxIterations) {
        var terminalState = Stream.iterate(
                LoopState.initial(initialRequest.messages()),
                state -> !state.terminal(),
                state -> step(client, initialRequest, tools, state, maxIterations))
                .reduce((first, second) -> second)
                .orElse(LoopState.initial(initialRequest.messages()));

        return toResult(terminalState, maxIterations);
    }

    /**
     * Single step of the tool loop - function from state to state.
     * Side-effects are confined to LLM calls and tool execution.
     */
    private static LoopState step(
            LlmClient client,
            LlmRequest template,
            ToolRegistry tools,
            LoopState state,
            int maxIterations) {
        if (state.iteration() >= maxIterations) {
            return state.withTerminal(state.lastResponse(), state.usage());
        }

        var request = template.withMessages(state.messages());
        var response = client.chat(request);
        var newUsage = state.usage().plus(response.usage());

        if (!response.hasToolCalls()) {
            return state.withTerminal(response, newUsage);
        }

        var toolCallRequests = response.message().toolCalls();

        try {
            var executionResult = executeToolCalls(toolCallRequests, tools);

            var newMessages = concat(state.messages(),
                    List.of((ChatMessage) response.message()),
                    executionResult.messages().stream()
                            .map(r -> (ChatMessage) r)
                            .toList());

            var newToolCalls = concat(state.toolCalls(), executionResult.records());

            return new LoopState(newMessages, newToolCalls, newUsage,
                    state.iteration() + 1, response, false, null);
        } catch (ToolExecutionInterruptedException e) {
            return state.withInterrupt(e.getMessage());
        }
    }

    private static ToolLoopResult toResult(LoopState state, int maxIterations) {
        if (state.interruptReason() != null) {
            return new ToolLoopResult.Interrupted(
                    state.usage(), state.iteration(), state.toolCalls(), state.interruptReason());
        }
        if (state.iteration() >= maxIterations && state.lastResponse() != null
                && state.lastResponse().hasToolCalls()) {
            return new ToolLoopResult.MaxIterationsReached(
                    state.lastResponse(), state.usage(), state.iteration(), state.toolCalls());
        }
        return new ToolLoopResult.Success(
                state.lastResponse(), state.usage(), state.iteration(), state.toolCalls());
    }

    private static ToolExecutionResult executeToolCalls(
            List<ToolCallRequest> toolCallRequests,
            ToolRegistry tools) {
        if (toolCallRequests.size() == 1) {
            var tc = toolCallRequests.getFirst();
            var result = executeSingleTool(tc, tools);
            return new ToolExecutionResult(List.of(result.message()), List.of(result.record()));
        }

        try (var scope = StructuredTaskScope.open()) {
            var subtasks = toolCallRequests.stream()
                    .map(tc -> scope.fork(() -> executeSingleTool(tc, tools)))
                    .toList();
            scope.join();

            var messages = subtasks.stream()
                    .map(s -> s.get().message())
                    .toList();
            var records = subtasks.stream()
                    .map(s -> s.get().record())
                    .toList();
            return new ToolExecutionResult(messages, records);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ToolExecutionInterruptedException("Tool execution interrupted", e);
        }
    }

    private record SingleToolResult(ChatMessage.ToolResult message, ToolCallRecord record) {
    }

    private static SingleToolResult executeSingleTool(ToolCallRequest tc, ToolRegistry tools) {
        var start = Instant.now();
        var tool = tools.findByName(tc.functionName());
        String resultContent;
        if (tool.isPresent()) {
            LOG.debug("Executing tool '{}' with args: {}", tc.functionName(), tc.argumentsJson());
            try {
                resultContent = tool.get().executor().execute(tc.argumentsJson());
            } catch (Exception e) {
                LOG.warn("Tool '{}' failed: {}", tc.functionName(), e.getMessage());
                resultContent = "Error: " + e.getMessage();
            }
        } else {
            LOG.warn("Tool '{}' not found in registry", tc.functionName());
            resultContent = "Error: tool '" + tc.functionName() + "' not found";
        }
        var duration = Duration.between(start, Instant.now());
        var record = new ToolCallRecord(tc.functionName(), tc.argumentsJson(), resultContent, duration);
        var message = new ChatMessage.ToolResult(tc.id(), resultContent);
        return new SingleToolResult(message, record);
    }

    @SafeVarargs
    private static <T> List<T> concat(List<T>... lists) {
        return java.util.stream.Stream.of(lists)
                .flatMap(List::stream)
                .toList();
    }

    /**
     * Checked-semantics exception for interrupted tool execution -
     * caught and converted to {@link ToolLoopResult.Interrupted} by the loop.
     */
    private static final class ToolExecutionInterruptedException extends RuntimeException {
        ToolExecutionInterruptedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private ToolLoop() {
    }
}
