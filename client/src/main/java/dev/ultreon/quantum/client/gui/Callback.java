package dev.ultreon.quantum.client.gui;

@FunctionalInterface
public interface Callback<T> {
    void call(T caller);

    default void call0(Object caller) {
        this.call((T) caller);
    }
}
