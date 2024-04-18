package com.ultreon.quantum.util;

public class IllegalThreadInterruptionError extends Error {
    public IllegalThreadInterruptionError(String message) {
        super(message);
    }

    public IllegalThreadInterruptionError(String message, InterruptedException cause) {
        super(message, cause);
    }
}
