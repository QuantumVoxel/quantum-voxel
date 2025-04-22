package dev.ultreon.quantum.network;

public class EncoderException extends RuntimeException {
    public EncoderException(Throwable cause) {
        super(cause);
    }

    public EncoderException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncoderException(String message) {
        super(message);
    }

    public EncoderException() {
    }
}
