package dev.ultreon.quantum.util;

public class TerminationFailedException extends RuntimeException {
    public TerminationFailedException() {
        super();
    }

    public TerminationFailedException(String message) {
        super(message);
    }

    public TerminationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TerminationFailedException(Throwable cause) {
        super(cause);
    }
}
