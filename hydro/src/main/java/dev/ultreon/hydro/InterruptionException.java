package dev.ultreon.hydro;

public class InterruptionException extends Exception {
    public InterruptionException() {
        super();
    }

    public InterruptionException(String message) {
        super(message);
    }

    public InterruptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterruptionException(Throwable cause) {
        super(cause);
    }
}
