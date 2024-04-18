package com.ultreon.quantum.util;

public class SanityCheckException extends RuntimeException {
    public SanityCheckException() {
        super();
    }

    public SanityCheckException(String message) {
        super(message);
    }

    public SanityCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public SanityCheckException(Throwable cause) {
        super(cause);
    }
}
