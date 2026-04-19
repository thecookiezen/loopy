package com.loopy.core.agent;

import com.loopy.core.TypeToken;
import com.loopy.core.condition.Condition;
import com.loopy.core.condition.Precondition;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Defines what the agent wants to achieve.
 */
public record GoalDefinition(
        String name,
        String description,
        Set<Precondition> preconditions,
        TypeToken<?> outputType,
        double value) {
    public GoalDefinition {
        var allPreconditions = new LinkedHashSet<Precondition>(preconditions);
        allPreconditions.add(Precondition.requires(Condition.typePresent(outputType)));
        preconditions = Collections.unmodifiableSet(Set.copyOf(preconditions));

        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("Value must be between 0.0 and 1.0, got: " + value);
        }
    }
}
