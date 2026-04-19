package com.loopy.runtime;

import com.loopy.core.TokenUsage;
import com.loopy.core.llm.LlmResponse;

import java.util.List;

/**
 * Terminal outcome of the {@link ToolLoop} the iterative LLM tool-calling cycle.
 *
 * Hierarchy representing three possible end states:
 * - {@link Success} - the LLM returned a final response without tool calls
 * - {@link Interrupted} - tool execution was interrupted
 * - {@link MaxIterationsReached} - the loop exceeded the iteration limit while the LLM was still requesting tool calls
 */
public sealed interface ToolLoopResult {

    /**
     * Total token usage accumulated across all LLM calls in the loop.
     *
     * @return the accumulated token usage
     */
    TokenUsage accumulatedUsage();

    /**
     * Number of tool-call iterations completed.
     *
     * @return the iteration count
     */
    int iterations();

    /**
     * Records of all tool calls made during the loop.
     *
     * @return the list of tool call records
     */
    List<ToolCallRecord> toolCalls();

    /**
     * The LLM produced a final text response (no further tool calls).
     *
     * @param finalResponse    the last LLM response containing the answer
     * @param accumulatedUsage total token usage across all iterations
     * @param iterations       number of tool-call iterations completed
     * @param toolCalls        records of all tool calls made
     */
    record Success(
                    LlmResponse finalResponse,
                    TokenUsage accumulatedUsage,
                    int iterations,
                    List<ToolCallRecord> toolCalls) implements ToolLoopResult {
    }

    /**
     * Tool execution was interrupted before completion.
     *
     * @param accumulatedUsage total token usage before interruption
     * @param iterations       number of iterations completed before interruption
     * @param toolCalls        records of tool calls completed before interruption
     * @param reason           description of why execution was interrupted
     */
    record Interrupted(
                    TokenUsage accumulatedUsage,
                    int iterations,
                    List<ToolCallRecord> toolCalls,
                    String reason) implements ToolLoopResult {
    }

    /**
     * The loop hit the iteration limit while the LLM was still requesting tool calls.
     *
     * @param lastResponse     the last LLM response (still containing tool calls)
     * @param accumulatedUsage total token usage across all iterations
     * @param iterations       number of iterations when the limit was reached
     * @param toolCalls        records of all tool calls made
     */
    record MaxIterationsReached(
                    LlmResponse lastResponse,
                    TokenUsage accumulatedUsage,
                    int iterations,
                    List<ToolCallRecord> toolCalls) implements ToolLoopResult {
    }
}
