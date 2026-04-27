package com.loopy.core.tool;

import com.loopy.core.llm.ToolCallRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

/**
 * Default implementation of {@link ToolCallExecutor} that resolves tools
 * from a {@link ToolRegistry}, executes them, and handles errors.
 *
 * Lookup failures and execution exceptions are caught and converted to
 * error messages in the result content, so the caller never needs to handle
 * raw exceptions from tool execution.
 */
public final class DefaultToolCallExecutor implements ToolCallExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultToolCallExecutor.class);

    public static final DefaultToolCallExecutor INSTANCE = new DefaultToolCallExecutor();

    @Override
    public ToolCallResult execute(ToolCallRequest request, ToolRegistry registry) {
        var start = Instant.now();
        var tool = registry.findByName(request.functionName());
        String resultContent;
        if (tool.isPresent()) {
            LOG.debug("Executing tool '{}' with args: {}", request.functionName(), request.argumentsJson());
            try {
                resultContent = tool.get().executor().execute(request.argumentsJson());
            } catch (Exception e) {
                LOG.warn("Tool '{}' failed: {}", request.functionName(), e.getMessage());
                resultContent = "Error: " + e.getMessage();
            }
        } else {
            LOG.warn("Tool '{}' not found in registry", request.functionName());
            resultContent = "Error: tool '" + request.functionName() + "' not found";
        }
        var duration = Duration.between(start, Instant.now());
        return new ToolCallResult(request.id(), request.functionName(),
                request.argumentsJson(), resultContent, duration);
    }

    private DefaultToolCallExecutor() {
    }
}
