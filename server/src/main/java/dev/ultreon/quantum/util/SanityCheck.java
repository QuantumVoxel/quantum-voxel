package dev.ultreon.quantum.util;

public class SanityCheck extends Error {
    public SanityCheck() {
        super();
    }

    public SanityCheck(String message) {
        super(message);
    }

    public SanityCheck(String message, Throwable cause) {
        super(message, cause);
    }

    public SanityCheck(Throwable cause) {
        super(cause);
    }
}
