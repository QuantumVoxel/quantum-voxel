package dev.ultreon.quantum.api.neocommand;

public class CommandExecuteException extends RuntimeException {
    public CommandExecuteException() {
    }

    public CommandExecuteException(String message) {
        super(message);
    }

    public CommandExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandExecuteException(Throwable cause) {
        super(cause);
    }
}
