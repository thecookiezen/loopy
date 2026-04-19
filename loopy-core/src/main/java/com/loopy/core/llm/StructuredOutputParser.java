package com.loopy.core.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Parses raw LLM text output into typed Java objects using Jackson.
 *
 * Handles common LLM output quirks such as markdown code block wrapping
 * ({@code ```json ... ```}). Throws {@link LlmParsingException} when parsing
 * fails, preserving the raw content for debugging.
 *
 * @see LlmClient#createObject(LlmRequest, Class)
 */
public final class StructuredOutputParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Parse raw LLM output into a typed object.
     *
     * Automatically strips markdown code block delimiters before parsing.
     *
     * @param content the raw LLM text output
     * @param type    the target Java type
     * @param <T>     the type of the parsed object
     * @return the deserialized object
     * @throws LlmParsingException if the content cannot be parsed as the given type
     */
    public static <T> T parse(String content, Class<T> type) {
        var cleaned = stripMarkdownCodeBlock(content);
        try {
            return MAPPER.readValue(cleaned, type);
        } catch (JsonProcessingException e) {
            throw new LlmParsingException("Failed to parse LLM response as " + type.getSimpleName(), content, e);
        }
    }

    private static String stripMarkdownCodeBlock(String content) {
        var trimmed = content.strip();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.strip();
    }

    /**
     * Exception thrown when structured output parsing fails.
     * Preserves the raw LLM content for inspection and debugging.
     */
    public static final class LlmParsingException extends RuntimeException {
        private final String rawContent;

        /**
         * @param message    description of the parsing failure
         * @param rawContent the raw content that failed to parse
         * @param cause      the underlying parsing exception
         */
        public LlmParsingException(String message, String rawContent, Throwable cause) {
            super(message, cause);
            this.rawContent = rawContent;
        }

        /**
         * Return the raw LLM content that failed to parse.
         *
         * @return the unparsed content
         */
        public String rawContent() {
            return rawContent;
        }
    }

    private StructuredOutputParser() {
    }
}
