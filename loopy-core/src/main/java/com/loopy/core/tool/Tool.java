package com.loopy.core.tool;

/**
 * Describes a tool that can be invoked by an LLM during a chat completion.
 *
 * Corresponds to the OpenAI function-calling tool format. The
 * {@code parametersSchemaJson} follows JSON Schema and is typically generated
 * by {@link com.loopy.core.json.JsonSchemaGenerator}.
 *
 * @param name                 the unique function name
 * @param description          a human-readable description sent to the LLM
 * @param parametersSchemaJson the JSON Schema describing the function parameters
 * @param executor             the function that executes the tool call
 */
public record Tool(String name, String description, String parametersSchemaJson, ToolExecutor executor) {
}
