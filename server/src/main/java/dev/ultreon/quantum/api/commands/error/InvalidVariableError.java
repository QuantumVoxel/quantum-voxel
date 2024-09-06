package dev.ultreon.quantum.api.commands.error;

public class InvalidVariableError extends InvalidError {
    public InvalidVariableError() {
        super("Invalid variable");
    }

    public InvalidVariableError(String message) {
        super("Invalid variable: " + message);
    }
}
