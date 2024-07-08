package dev.ultreon.quantum.util;

/**
 * Interface for objects that can be copied.
 *
 * @param <T> the type of the object
 */
public interface Copyable<T> {
    T cpy();
}
