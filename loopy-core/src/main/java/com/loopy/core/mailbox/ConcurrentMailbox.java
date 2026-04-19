package com.loopy.core.mailbox;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.loopy.core.TypeToken;

/**
 * Mutable Mailbox for use during agent execution.
 */
public final class ConcurrentMailbox implements Mailbox {

    private final CopyOnWriteArrayList<Object> entries;
    private final ConcurrentHashMap<String, Object> named;

    private ConcurrentMailbox(List<Object> entries, Map<String, Object> named) {
        this.entries = new CopyOnWriteArrayList<>(entries);
        this.named = new ConcurrentHashMap<>(named);
    }

    /**
     * Creates an empty concurrent mailbox.
     *
     * @return an empty concurrent mailbox
     */
    public static ConcurrentMailbox empty() {
        return new ConcurrentMailbox(List.of(), Map.of());
    }

    /**
     * Creates a concurrent mailbox from an existing mailbox.
     * Copies all messages from the source mailbox.
     *
     * @param other the mailbox to copy from
     * @return a new concurrent mailbox with the same messages
     */
    public static ConcurrentMailbox from(Mailbox other) {
        return new ConcurrentMailbox(other.messages(), Map.of());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> last(TypeToken<T> type) {
        for (Object o: entries) {
            if (type.rawType().isInstance(o)) {
                return Optional.of((T) o);
            }
        }
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> allOfType(TypeToken<T> type) {
        return entries.stream()
                .filter(type.rawType()::isInstance)
                .map(e -> (T) e)
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String name, TypeToken<T> type) {
        return Optional.ofNullable(named.get(name))
                .filter(type.rawType()::isInstance)
                .map(v -> (T) v);
    }

    @Override
    public List<Object> messages() {
        return List.copyOf(entries);
    }

    @Override
    public Mailbox post(Object value) {
        entries.add(value);
        return this;
    }

    @Override
    public Mailbox bind(String name, Object value) {
        named.put(name, value);
        entries.add(value);
        return this;
    }
}
