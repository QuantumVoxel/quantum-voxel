package dev.ultreon.quantum.network;

public class ServerHostingException extends ServerStatusException {
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
