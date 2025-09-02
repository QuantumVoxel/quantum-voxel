package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.neocommand.params.ArgumentType;

import java.util.Objects;

/**
 * @param <T> the type of the argument associated with this parameter
 */
public final class Parameter<T> {
    private final String name;
    private final ArgumentType<T> type;

    /**
     * Constructs a new Parameter instance with the specified name and argument type.
     *
     * @param name the name of the parameter; must not be null
     * @param type the type of the argument associated with this parameter; must not be null
     */
    public Parameter(
            String name,
            ArgumentType<T> type
    ) {
        this.name = name;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public ArgumentType<T> type() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Parameter<?>) obj;
        return Objects.equals(this.name, that.name) &&
               Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "Parameter[" +
               "name=" + name + ", " +
               "type=" + type + ']';
    }
}
