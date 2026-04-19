package com.loopy.core.llm;

import com.loopy.core.TokenUsage;

/**
 * Pricing model for computing the cost of an LLM API call.
 *
 * Stores per-million-token pricing for input and output tokens and provides
 * a method to compute the cost of a given {@link TokenUsage}.
 *
 * @param modelName                  the model identifier
 * @param inputPricePerMillionTokens  price per million input (prompt) tokens in USD
 * @param outputPricePerMillionTokens price per million output (completion) tokens in USD
 * @see TokenUsage
 */
public record PricingModel(
        String modelName,
        double inputPricePerMillionTokens,
        double outputPricePerMillionTokens) {

    /**
     * Compute the cost of the given token usage using this pricing model.
     *
     * @param usage the token usage to price
     * @return the cost in USD
     */
    public double costOf(TokenUsage usage) {
        return (usage.promptTokens() * inputPricePerMillionTokens / 1_000_000.0)
                + (usage.completionTokens() * outputPricePerMillionTokens / 1_000_000.0);
    }

    public static final PricingModel GPT_5_4 = new PricingModel("gpt-5.4", 2.50, 15.00);
    public static final PricingModel GPT_5_4_MINI = new PricingModel("gpt-5.4-mini", 0.75, 4.50);
    public static final PricingModel GPT_5_4_NANO = new PricingModel("gpt-5.4-nano", 0.20, 1.25);
}
