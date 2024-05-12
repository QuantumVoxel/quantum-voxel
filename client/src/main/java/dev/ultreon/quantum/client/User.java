package dev.ultreon.quantum.client;

import java.util.Objects;

/**
 * Represents a user in the game.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public final class User {
    private final String name;

    /**
     * @param name the name of the user
     */
    public User(String name) {
        this.name = name;
    }

    /**
     * Returns a string representation of the user.
     *
     * @return the name of the user
     */
    @Override
    public String toString() {
        return name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (User) obj;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
