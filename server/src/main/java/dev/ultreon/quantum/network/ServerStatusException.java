package dev.ultreon.quantum.network;

import java.io.IOException;

public class ServerStatusException extends IOException {
    public ServerStatusException() {
        super();
    }

    public ServerStatusException(String s) {
        super(s);
    }

    public ServerStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerStatusException(Throwable cause) {
        super(cause);
    }
}
