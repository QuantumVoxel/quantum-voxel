package dev.ultreon.quantum.util;

public class ValueExistsException extends RuntimeException {
    public ValueExistsException() {
    }

    public ValueExistsException(String message) {
        super(message);
    }

    public ValueExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueExistsException(Throwable cause) {
        super(cause);
    }
}
