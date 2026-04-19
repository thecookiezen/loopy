package com.loopy.core.mailbox;

import java.util.List;
import java.util.Optional;

import com.loopy.core.TypeToken;
import com.loopy.core.planning.BeliefDeriver;

/**
 * The agent's working memory. Actions communicate exclusively by posting
 * messages to the Mailbox.
 *
 * The Mailbox is a message store. Planning-layer concerns (condition flags,
 * belief derivation) are handled by {@link BeliefDeriver}.
 */
public sealed interface Mailbox permits ImmutableMailbox, ConcurrentMailbox {

    /**
     * Get the last message of a specific type.
     */
    <T> Optional<T> last(TypeToken<T> type);

    /**
     * Get all messages of a specific type.
     */
    <T> List<T> allOfType(TypeToken<T> type);

    /**
     * Get a named message, cast to the expected type.
     *
     * @param name the binding name
     * @param type the expected type of the message
     * @return the message if present and assignable to the given type
     */
    <T> Optional<T> get(String name, TypeToken<T> type);

    /**
     * Get all messages in the mailbox.
     */
    List<Object> messages();

    /**
     * Post a message to the mailbox.
     */
    Mailbox post(Object value);

    /**
     * Bind a name to a message in the mailbox.
     */
    Mailbox bind(String name, Object value);
}
