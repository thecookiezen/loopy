package com.loopy.core.action;

/**
 * Links an {@link ActionDefinition} (planning metadata) to its
 * executable behavior ({@link ActionBehavior}).
 */
public record ActionBinding(ActionDefinition definition, ActionBehavior behavior) {
}
