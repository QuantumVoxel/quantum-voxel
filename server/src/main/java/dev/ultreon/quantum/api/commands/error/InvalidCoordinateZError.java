package dev.ultreon.quantum.api.commands.error;

import org.jetbrains.annotations.NotNull;

@Deprecated
public class InvalidCoordinateZError extends InvalidCoordinateError {

    public InvalidCoordinateZError() {
        super("z");
    }

    public InvalidCoordinateZError(int index) {
        super("z", index);
    }

    public InvalidCoordinateZError(String got) {
        super("z", got);
    }

    public InvalidCoordinateZError(String got, int index) {
        super("z", got, index);
    }

    @Override
    public @NotNull String getName() {
        return "Invalid";
    }
}