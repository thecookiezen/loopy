package com.loopy.runtime;

import com.loopy.core.mailbox.Mailbox;

/**
 * Terminal outcome of an agent execution.
 */
public sealed interface AgentExecution {

    /**
     * The agent successfully reached its goal.
     *
     * @param result     the final output value (may be {@code null})
     * @param finalState the mailbox state at completion
     * @param trace      the full execution trace for auditing
     */
    record Completed(Object result, Mailbox finalState, ExecutionTrace trace) implements AgentExecution {
    }

    /**
     * The agent could not make further progress toward its goal.
     *
     * @param state the mailbox state when the agent got stuck
     * @param trace the execution trace so far
     * @param reason human-readable explanation of why the agent is stuck
     */
    record Stuck(Mailbox state, ExecutionTrace trace, String reason) implements AgentExecution {
    }

    /**
     * The agent failed due to an unrecoverable error.
     *
     * @param cause the exception that caused the failure
     * @param trace the execution trace up to the failure point
     */
    record Failed(Exception cause, ExecutionTrace trace) implements AgentExecution {
    }
}
