package dev.ultreon.quantum.api.commands;

import java.util.Objects;

/**
 * Represents the context of a command with a specified name.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public final class CommandContext {
    private final String name;

    /**
     * @param name the name of the command
     */
    public CommandContext(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CommandContext) obj;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "CommandContext[" +
               "name=" + name + ']';
    }


}
