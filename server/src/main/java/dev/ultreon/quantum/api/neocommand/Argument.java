package dev.ultreon.quantum.api.neocommand;

import java.util.Objects;

public final class Argument<T> {
    private final Class<? extends T> type;
    private final String name;
    private final T value;

    public Argument(Class<? extends T> type, String name, T value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public Class<? extends T> type() {
        return type;
    }

    public String name() {
        return name;
    }

    public T value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Argument) obj;
        return Objects.equals(this.type, that.type) &&
               Objects.equals(this.name, that.name) &&
               Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, value);
    }

    @Override
    public String toString() {
        return "Argument[" +
               "type=" + type + ", " +
               "name=" + name + ", " +
               "value=" + value + ']';
    }

}
