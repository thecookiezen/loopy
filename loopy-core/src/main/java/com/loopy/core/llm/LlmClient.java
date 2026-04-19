package com.loopy.core.llm;

/**
 * Port for interacting with a Large Language Model.
 *
 * Provides two modes of interaction:
 * - {@link #chat(LlmRequest)} - free-form chat completion returning
 *   an {@link LlmResponse} that may contain text and/or tool call requests
 * - {@link #createObject(LlmRequest, Class)} - structured output
 *   generation, deserializing the LLM response into a typed Java object
 *
 * @see LlmRequest
 * @see LlmResponse
 * @see TrackingLlmClient
 */
public interface LlmClient {

    /**
     * Send a chat completion request and return the response.
     *
     * @param request the chat request containing messages, tools, and parameters
     * @return the LLM response, which may contain text content and/or tool calls
     */
    LlmResponse chat(LlmRequest request);

    /**
     * Send a request and deserialize the response into a typed Java object.
     *
     * Uses structured output / JSON mode to ensure the LLM response
     * conforms to the requested type.
     *
     * @param request    the request containing messages and parameters
     * @param outputType the target Java type to deserialize into
     * @param <T>        the type of the structured output
     * @return a result containing the deserialized value and token usage
     */
    <T> LlmObjectResult<T> createObject(LlmRequest request, Class<T> outputType);
}
