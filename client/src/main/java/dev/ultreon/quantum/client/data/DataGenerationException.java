package dev.ultreon.quantum.client.data;

public class DataGenerationException extends RuntimeException {
    public DataGenerationException() {
        super();
    }

    public DataGenerationException(String message) {
        super(message);
    }

    public DataGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataGenerationException(Throwable cause) {
        super(cause);
    }
}
