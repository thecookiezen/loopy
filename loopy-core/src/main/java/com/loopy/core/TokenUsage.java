package com.loopy.core;

/**
 * Record tracking token consumption across LLM API calls.
 *
 * Used to accumulate prompt and completion token counts during agent
 * execution. Instances are combined via {@link #plus(TokenUsage)} and
 * tracked by {@link com.loopy.core.llm.TrackingLlmClient}.
 *
 * @param promptTokens     the number of tokens in the prompt
 * @param completionTokens the number of tokens in the completion
 */
public record TokenUsage(int promptTokens, int completionTokens) {

    /**
     * Return the total number of tokens (prompt + completion).
     *
     * @return the total token count
     */
    public int totalTokens() {
        return promptTokens + completionTokens;
    }

    /**
     * Combine this usage with another, summing both token counts.
     *
     * @param other the other usage to add
     * @return a new usage with summed counts
     */
    public TokenUsage plus(TokenUsage other) {
        return new TokenUsage(
                this.promptTokens + other.promptTokens,
                this.completionTokens + other.completionTokens
        );
    }

    /**
     * Return a zero-usage instance.
     *
     * @return a token usage with zero prompt and completion tokens
     */
    public static TokenUsage zero() {
        return new TokenUsage(0, 0);
    }
}
