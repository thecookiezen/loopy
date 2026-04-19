package com.loopy.core.agent;

import java.util.Objects;
import java.util.UUID;

/**
 * A unique, auto-generated identifier for an agent execution instance.
 *
 * Each agent run receives its own {@link AgentId}, enabling the system to
 * correlate external signals (e.g. human-in-the-loop responses) with the
 * specific agent instance that is waiting for them.
 */
public record AgentId(String value) {

    public AgentId {
        Objects.requireNonNull(value, "AgentId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("AgentId value must not be blank");
        }
    }

    public static AgentId generate() {
        return new AgentId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
