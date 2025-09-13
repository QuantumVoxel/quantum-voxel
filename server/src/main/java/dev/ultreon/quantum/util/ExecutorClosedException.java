package dev.ultreon.quantum.util;

public class ExecutorClosedException extends RejectedExecutionException {
    public ExecutorClosedException(String message) {
        super(message);
    }
}
