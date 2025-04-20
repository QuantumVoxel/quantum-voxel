package dev.ultreon.quantum.api.commands;

@Deprecated
public class OverloadConflictException extends RuntimeException {
    private final String[] aliases;

    public OverloadConflictException(String... aliases) {
        this.aliases = aliases;
    }

    public String[] getAliases() {
        return this.aliases;
    }
}
