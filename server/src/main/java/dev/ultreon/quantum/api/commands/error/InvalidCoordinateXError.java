package dev.ultreon.quantum.api.commands.error;

import org.jetbrains.annotations.NotNull;

@Deprecated
public class InvalidCoordinateXError extends InvalidCoordinateError {
    private String name = "Invalid";

    public InvalidCoordinateXError() {
        super("x");
    }

    public InvalidCoordinateXError(int index) {
        super("x", index);
    }

    public InvalidCoordinateXError(String got) {
        super("x", got);
    }

    public InvalidCoordinateXError(String got, int index) {
        super("x", got, index);
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }
}