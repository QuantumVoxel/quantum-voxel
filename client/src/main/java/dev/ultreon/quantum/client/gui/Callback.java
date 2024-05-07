package dev.ultreon.quantum.client.gui;

@FunctionalInterface
public interface Callback<T> {
    void call(T caller);
}
