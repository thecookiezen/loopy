package com.loopy.core.agent;

import com.loopy.core.TokenUsage;
import com.loopy.core.action.ActionDefinition;
import com.loopy.core.action.ActionInputs;
import com.loopy.core.llm.*;
import com.loopy.core.mailbox.Mailbox;
import com.loopy.core.planning.Beliefs;
import com.loopy.core.planning.Plan;
import com.loopy.core.tool.ToolCallResult;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public sealed interface DebugEvent extends AgentLifecycleEvent {

    record LlmRequestSent(LlmRequest request, Instant timestamp) implements DebugEvent {
    }

    record LlmResponseReceived(String model, LlmResponse response, Duration duration,
                                Instant timestamp) implements DebugEvent {
    }

    record LlmObjectRequestSent(LlmRequest request, Class<?> outputType,
                                 Instant timestamp) implements DebugEvent {
    }

    record LlmObjectResultReceived(Class<?> outputType, LlmObjectResult<?> result,
                                    Duration duration, Instant timestamp) implements DebugEvent {
    }

    record ToolCallExecuted(ToolCallRequest request, ToolCallResult result,
                             Duration duration, Instant timestamp) implements DebugEvent {
    }

    record ToolLoopIteration(int iteration, int maxIterations,
                              List<ChatMessage> messages,
                              Instant timestamp) implements DebugEvent {
    }

    record ToolLoopCompleted(Object result, int totalIterations,
                              TokenUsage accumulatedUsage,
                              Instant timestamp) implements DebugEvent {
    }

    record PlanningStarted(Beliefs beliefs, Set<ActionDefinition> actions,
                            GoalDefinition goal,
                            Instant timestamp) implements DebugEvent {
    }

    record PlanningNodeExpanded(Beliefs beliefState, double gScore, double hScore,
                                 ActionDefinition actionApplied,
                                 Instant timestamp) implements DebugEvent {
    }

    record PlanningCompleted(Plan plan, int iterations,
                              Instant timestamp) implements DebugEvent {
    }

    record PlanningFailed(Beliefs beliefs, int iterations,
                           Instant timestamp) implements DebugEvent {
    }

    record OodaCycleStarted(int stepNumber, Mailbox mailbox, Beliefs beliefs,
                             Instant timestamp) implements DebugEvent {
    }

    record ActionInputResolved(ActionDefinition action, ActionInputs inputs,
                                Instant timestamp) implements DebugEvent {
    }
}
