package dev.ultreon.xeox.impl;

public class ModLoaderException extends RuntimeException {
    public ModLoaderException() {
        super();
    }

    public ModLoaderException(String message) {
        super(message);
    }

    public ModLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModLoaderException(Throwable cause) {
        super(cause);
    }
}
