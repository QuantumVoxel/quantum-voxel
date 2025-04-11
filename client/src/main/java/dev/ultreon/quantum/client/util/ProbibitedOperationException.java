package dev.ultreon.quantum.client.util;

public class ProbibitedOperationException extends Exception {
    public ProbibitedOperationException() {
        super("Operation not allowed");
    }

    public ProbibitedOperationException(String message) {
        super(message);
    }
}
