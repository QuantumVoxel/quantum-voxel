package com.ultreon.quantum.network.system;

public class ReadOnlyConnectionException extends RuntimeException {
    public ReadOnlyConnectionException() {
        super("Connection is read-only");
    }

    public ReadOnlyConnectionException(String message) {
        super(message);
    }
}
