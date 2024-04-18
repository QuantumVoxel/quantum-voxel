package com.ultreon.quantum.collection;

public class PaletteIndexException extends RuntimeException {
    public PaletteIndexException() {
        super();
    }

    public PaletteIndexException(String message) {
        super(message);
    }

    public PaletteIndexException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaletteIndexException(Throwable cause) {
        super(cause);
    }
}
