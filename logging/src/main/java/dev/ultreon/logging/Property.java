package dev.ultreon.logging;

public class Property<T> {
    private T value;

    public Property(T value) {
        this.value = value;
    }

    public Property() {
        this(null);
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
