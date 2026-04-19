package com.loopy.runtime;

import java.time.Duration;

/**
 * Audit record of a single tool invocation within the {@link ToolLoop}.
 *
 * Captures what was called, with what arguments, what was returned,
 * and how long it took. Used for execution tracing and debugging.
 *
 * @param toolName  the name of the tool that was invoked
 * @param arguments the JSON-encoded arguments passed to the tool
 * @param result    the result returned by the tool (or error message)
 * @param duration  the wall-clock duration of the tool execution
 */
public record ToolCallRecord(String toolName, String arguments, String result, Duration duration) {
}
