package com.loopy.core.action;

import com.loopy.core.TypeToken;
import com.loopy.core.mailbox.Mailbox;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Container holding the resolved inputs for an action.
 *
 * Built from {@link ActionDefinition#inputTypes()} and
 * the current {@link Mailbox} state.
 *
 * This avoids returning raw {@code Object} and eliminates the need for the
 * action to know which position maps to which type.
 */
public final class ActionInputs {

    private final Map<Class<?>, Object> entries;

    private ActionInputs(Map<Class<?>, Object> entries) {
        this.entries = Map.copyOf(entries);
    }

    /**
     * Retrieves the input value for the given type.
     *
     * @param type the input class
     * @param <T>  the input type
     * @return the resolved input, never null
     * @throws IllegalArgumentException if the requested type was not declared as an
     *                                  input
     */
    public <T> T get(Class<T> type) {
        var value = entries.get(type);
        if (value == null) {
            throw new IllegalArgumentException(
                    "No input of type %s - declared inputs are: %s"
                            .formatted(type.getSimpleName(), typeNames()));
        }
        return type.cast(value);
    }

    /**
     * Returns the number of resolved inputs.
     * 
     * @return the number of resolved inputs
     */
    public int size() {
        return entries.size();
    }

    /**
     * Resolves all declared input types from the mailbox, failing fast if any are
     * missing.
     *
     * @param action  the action whose input types to resolve
     * @param mailbox the mailbox to extract messages from
     * @return a fully populated {@code ActionInputs}
     * @throws IllegalStateException if a declared input type has no matching
     *                               message in the mailbox
     */
    public static ActionInputs resolve(ActionDefinition action, Mailbox mailbox) {
        Map<Class<?>, Object> resolved = action.inputTypes().stream()
                .collect(Collectors.toUnmodifiableMap(
                        TypeToken::rawType,
                        token -> mailbox.last(token)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Action '%s' expected input of type %s but none found in mailbox"
                                                .formatted(action.name(), token.simpleName())))));
        return new ActionInputs(resolved);
    }

    private String typeNames() {
        return entries.keySet().stream()
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public String toString() {
        return "ActionInputs" + typeNames();
    }
}
