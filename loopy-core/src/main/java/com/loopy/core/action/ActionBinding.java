package com.loopy.core.action;

/**
 * Links an {@link ActionDefinition} (planning metadata) to its
 * executable behavior ({@link ActionExecutor}).
 */
public record ActionBinding(ActionDefinition definition, ActionExecutor executor) {
}
