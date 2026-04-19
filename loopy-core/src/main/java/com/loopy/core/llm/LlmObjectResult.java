package com.loopy.core.llm;

import com.loopy.core.TokenUsage;

/**
 * Result of a structured output (JSON mode) LLM call.
 *
 * Contains the deserialized Java object and the token usage
 * consumed by the request.
 *
 * @param <T>   the type of the structured output
 * @param value the deserialized output object
 * @param usage token usage statistics for this request
 * @see LlmClient#createObject(LlmRequest, Class)
 */
public record LlmObjectResult<T>(T value, TokenUsage usage) {
}
