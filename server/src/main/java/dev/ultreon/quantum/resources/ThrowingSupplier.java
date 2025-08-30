package dev.ultreon.quantum.resources;

public interface ThrowingSupplier<T, E extends Throwable> {
    T get() throws E;
}
