package com.loopy.core.llm;

import com.loopy.core.TokenUsage;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Wraps an {@link LlmClient} and accumulates token usage
 * across all calls.
 *
 * Useful for tracking total token consumption during an agent execution
 * without modifying the underlying client.
 *
 * @see LlmClient
 * @see TokenUsage
 */
public final class TrackingLlmClient implements LlmClient {

    private final LlmClient delegate;
    private final AtomicReference<TokenUsage> accumulated = new AtomicReference<>(TokenUsage.zero());

    /**
     * Create a tracking wrapper around the given delegate client.
     *
     * @param delegate the underlying LLM client to delegate calls to
     */
    public TrackingLlmClient(LlmClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        var response = delegate.chat(request);
        accumulated.updateAndGet(current -> current.plus(response.usage()));
        return response;
    }

    @Override
    public <T> LlmObjectResult<T> createObject(LlmRequest request, Class<T> outputType) {
        var result = delegate.createObject(request, outputType);
        accumulated.updateAndGet(current -> current.plus(result.usage()));
        return result;
    }

    /**
     * Return the total accumulated token usage across all calls.
     *
     * @return the accumulated token usage
     */
    public TokenUsage accumulated() {
        return accumulated.get();
    }

    /**
     * Reset the accumulated usage to zero and return the previous value.
     *
     * @return the token usage before the reset
     */
    public TokenUsage reset() {
        return accumulated.getAndSet(TokenUsage.zero());
    }
}
