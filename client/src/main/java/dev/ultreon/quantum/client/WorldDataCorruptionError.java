package dev.ultreon.quantum.client;

public class WorldDataCorruptionError extends Error {
    public WorldDataCorruptionError() {
        super();
    }

    public WorldDataCorruptionError(String message) {
        super(message);
    }

    public WorldDataCorruptionError(String message, Throwable cause) {
        super(message, cause);
    }

    public WorldDataCorruptionError(Throwable cause) {
        super(cause);
    }
}
