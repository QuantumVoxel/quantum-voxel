package com.ultreon.quantum.network;

import java.io.IOException;

public class ServerHostingException extends IOException {
    public ServerHostingException() {
    }

    public ServerHostingException(String message) {
        super(message);
    }

    public ServerHostingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerHostingException(Throwable cause) {
        super(cause);
    }
}
