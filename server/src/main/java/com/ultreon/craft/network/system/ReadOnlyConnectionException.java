package com.ultreon.craft.network.system;

import java.io.IOException;

public class ReadOnlyConnectionException extends RuntimeException {
    public ReadOnlyConnectionException() {
        super("Connection is read-only");
    }

    public ReadOnlyConnectionException(String message) {
        super(message);
    }
}
