package com.loopy.core.mailbox;

import java.util.*;

import com.loopy.core.TypeToken;

/**
 * Persistent Mailbox - each mutation returns a new instance.
 * Ideal for functional pipelines and testing.
 */
public final class ImmutableMailbox implements Mailbox {

    private final List<Object> entries;
    private final Map<String, Object> named;

    private ImmutableMailbox(List<Object> entries, Map<String, Object> named) {
        this.entries = List.copyOf(entries);
        this.named = Map.copyOf(named);
    }

    public static ImmutableMailbox empty() {
        return new ImmutableMailbox(List.of(), Map.of());
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
        return entries;
    }

    @Override
    public Mailbox post(Object value) {
        var updated = new ArrayList<>(entries);
        updated.add(value);
        return new ImmutableMailbox(updated, named);
    }

    @Override
    public Mailbox bind(String name, Object value) {
        var updatedNamed = new HashMap<>(named);
        updatedNamed.put(name, value);
        var updatedEntries = new ArrayList<>(entries);
        updatedEntries.add(value);
        return new ImmutableMailbox(updatedEntries, updatedNamed);
    }

    @Override
    public String toString() {
        return "ImmutableMailbox{entries=%d, named=%s}"
                .formatted(entries.size(), named.keySet());
    }
}
