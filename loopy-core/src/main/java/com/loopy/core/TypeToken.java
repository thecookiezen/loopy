package com.loopy.core;

/**
 * Type token that captures a {@link Class} at the call site.
 *
 * Used throughout Loopy to represent input/output types in a way that
 * is compatible with record component declarations and collection generics.
 * Unlike {@code java.lang.reflect.Type}, this token only captures raw classes,
 * which is sufficient for the mailbox's type-based routing.
 *
 * @param <T>     the type represented by this token
 * @param rawType the underlying {@link Class} object
 */
public record TypeToken<T>(Class<T> rawType) {

    /**
     * Create a type token for the given class.
     *
     * @param clazz the class to tokenize
     * @param <T>   the type
     * @return a new type token
     */
    public static <T> TypeToken<T> of(Class<T> clazz) {
        return new TypeToken<>(clazz);
    }

    /**
     * Check if the given object is an instance of this token's type.
     *
     * @param obj the object to check
     * @return {@code true} if the object is assignable to this type
     */
    public boolean isAssignableFrom(Object obj) {
        return rawType.isInstance(obj);
    }

    /**
     * Return the simple name of the underlying class.
     *
     * @return the simple class name
     */
    public String simpleName() {
        return rawType.getSimpleName();
    }

    @Override
    public String toString() {
        return "TypeToken[" + rawType.getSimpleName() + "]";
    }
}
