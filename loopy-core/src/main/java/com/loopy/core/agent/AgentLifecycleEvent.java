package com.loopy.core.agent;

import com.loopy.core.action.ActionDefinition;
import com.loopy.core.action.ActionResult;
import com.loopy.core.mailbox.Mailbox;
import com.loopy.core.planning.Plan;
import com.loopy.core.planning.Beliefs;

import java.time.Duration;
import java.time.Instant;

/**
 * Events emitted during agent execution, allowing observation and integration
 * with external systems.
 *
 * Agents publish these events as they start, execute actions, replan, change
 * behavior, and finally complete or fail. Listeners can subscribe to these
 * events to monitor progress, implement human-in-the-loop handoffs, record
 * execution traces, or trigger side effects.
 */
public sealed interface AgentLifecycleEvent {
        Instant timestamp();

        /**
         * Emitted when an agent process starts.
         */
        record Started(AgentId agentId, AgentDefinition agent, GoalDefinition goal, Instant timestamp)
                        implements AgentLifecycleEvent {
        }

        /**
         * Emitted when an action starts executing.
         */
        record ActionStarted(ActionDefinition action, Mailbox state, Instant timestamp)
                        implements AgentLifecycleEvent {
        }

        /**
         * Emitted when an action completes successfully.
         */
        record ActionCompleted(ActionDefinition action, ActionResult result,
                        Duration duration, Instant timestamp) implements AgentLifecycleEvent {
        }

        /**
         * Emitted when an action fails during execution.
         */
        record ActionFailed(ActionDefinition action, Exception cause,
                        SupervisionStrategy appliedStrategy, Instant timestamp)
                        implements AgentLifecycleEvent {
        }

        /**
         * Emitted when the agent replans its strategy toward the goal.
         */
        record Replanned(Plan newPlan, Beliefs fromBeliefs, Instant timestamp)
                        implements AgentLifecycleEvent {
        }

        /**
         * Emitted when the agent's internal behavior changes.
         * Includes the previous behavior and the new behavior.
         */
        record BehaviorChanged(AgentBehavior from, AgentBehavior to, Instant timestamp)
                        implements AgentLifecycleEvent {
        }

        /**
         * Emitted when the agent process completes successfully.
         * Includes the final result and the timestamp of completion.
         */
        record ProcessCompleted(Object result, Instant timestamp)
                        implements AgentLifecycleEvent {
        }

        /**
         * Emitted when the agent process fails.
         * Includes the exception that caused the failure and the timestamp.
         */
        record ProcessFailed(Exception cause, Instant timestamp)
                        implements AgentLifecycleEvent {
        }
}
