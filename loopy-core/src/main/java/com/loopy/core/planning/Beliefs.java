package com.loopy.core.planning;

import com.loopy.core.condition.Condition;
import com.loopy.core.condition.Effect;
import com.loopy.core.condition.Precondition;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Set of conditions the agent believes to be true.
 * A condition's absence means it is not believed to hold.
 *
 * Used by the GOAP planner to reason about preconditions and effects.
 * Presence in the set means satisfied, absence means unsatisfied.
 *
 * Part of the BDI (Belief-Desire-Intention) triad:
 * Beliefs - what the agent believes to be true (this class)
 * Desires - what the agent wants to achieve ({@link GoalDefinition})
 * Intentions - the committed course of action ({@link Plan})
 */
public record Beliefs(Set<Condition> satisfiedConditions) {

    public Beliefs {
        satisfiedConditions = Collections.unmodifiableSet(new HashSet<>(satisfiedConditions));
    }

    public static Beliefs empty() {
        return new Beliefs(Set.of());
    }

    public Beliefs with(Condition condition) {
        var updated = new HashSet<>(satisfiedConditions);
        updated.add(condition);
        return new Beliefs(updated);
    }

    public Beliefs without(Condition condition) {
        var updated = new HashSet<>(satisfiedConditions);
        updated.remove(condition);
        return new Beliefs(updated);
    }

    /**
     * Check if all preconditions are satisfied by this belief state.
     */
    public boolean satisfies(Set<Precondition> required) {
        return required.stream().allMatch(this::isSatisfied);
    }

    /**
     * Check a single precondition against this belief state.
     */
    public boolean isSatisfied(Precondition precondition) {
        return switch (precondition) {
            case Precondition.Requires r -> satisfiedConditions.contains(r.condition());
            case Precondition.Forbids f -> !satisfiedConditions.contains(f.condition());
        };
    }

    /**
     * Apply a set of effects to produce a new belief state.
     */
    public Beliefs applyEffects(Set<Effect> effects) {
        var updated = new HashSet<>(satisfiedConditions);
        for (var effect : effects) {
            switch (effect) {
                case Effect.Establishes e -> updated.add(e.condition());
                case Effect.Revokes r -> updated.remove(r.condition());
            }
        }
        return new Beliefs(updated);
    }

    /**
     * Check if a specific condition is believed to hold.
     */
    public boolean holds(Condition condition) {
        return satisfiedConditions.contains(condition);
    }
}
