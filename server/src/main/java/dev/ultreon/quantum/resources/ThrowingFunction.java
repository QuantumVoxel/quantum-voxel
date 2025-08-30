package dev.ultreon.quantum.resources;

public interface ThrowingFunction<T, R, E extends Throwable> {
    R apply(T t) throws E;
}
