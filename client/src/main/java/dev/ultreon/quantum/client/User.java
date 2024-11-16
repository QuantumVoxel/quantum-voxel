package dev.ultreon.quantum.client;

import java.util.Objects;

/**
 * Represents a user with a name.
 * <p>
 * The User record provides implementations for toString and equals methods.
 */
public record User(String name) {

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (User) obj;
        return Objects.equals(this.name, that.name);
    }

}
