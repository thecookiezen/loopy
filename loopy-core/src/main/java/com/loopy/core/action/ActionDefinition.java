package com.loopy.core.action;

import com.loopy.core.TypeToken;
import com.loopy.core.condition.Condition;
import com.loopy.core.condition.Effect;
import com.loopy.core.condition.Precondition;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Metadata describing an action's contract.
 * Contains preconditions, effects, input/output types, and cost, everything
 * the planner needs to reason about the action without executing it.
 *
 * The executable behavior is provided separately via {@link ActionBinding}
 * which pairs this with an {@link ActionBehavior}.
 */
public record ActionDefinition(
        String name,
        String description,
        Set<Precondition> preconditions,
        Set<Effect> effects,
        Set<TypeToken<?>> inputTypes,
        Set<TypeToken<?>> outputTypes,
        double cost,
        boolean canRerun) {
    public ActionDefinition {
        var allPreconditions = new LinkedHashSet<Precondition>(preconditions);
        for (var type : inputTypes) {
            allPreconditions.add(Precondition.requires(Condition.typePresent(type)));
        }
        preconditions = Collections.unmodifiableSet(Set.copyOf(allPreconditions));

        var allEffects = new LinkedHashSet<Effect>(effects);
        for (var type : outputTypes) {
            allEffects.add(Effect.establishes(Condition.typePresent(type)));
        }
        effects = Collections.unmodifiableSet(Set.copyOf(allEffects));

        inputTypes = Collections.unmodifiableSet(Set.copyOf(inputTypes));
        outputTypes = Collections.unmodifiableSet(Set.copyOf(outputTypes));

        if (cost < 0.0 || cost > 1.0) {
            throw new IllegalArgumentException("Cost must be between 0.0 and 1.0, got: " + cost);
        }
    }
}
