package dev.ultreon.quantum.client;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The User class represents a record with a single immutable property, `name`.
 * This record is used to encapsulate user information with methods for
 * string representation and equality checking.
 *
 */
public final class User {
    private final String name;

    /**
     * @param name the name of the user
     */
    public User(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (User) obj;
        return Objects.equals(this.name, that.name);
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


}
