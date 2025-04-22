package dev.ultreon.quantum;

public class AsyncException extends RuntimeException {
    public AsyncException(Throwable throwable) {
        super(throwable);
    }

    public AsyncException(String message) {
        super(message);
    }
}
