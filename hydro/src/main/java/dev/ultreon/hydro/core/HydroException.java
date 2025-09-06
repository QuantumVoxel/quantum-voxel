package dev.ultreon.hydro.core;

public class HydroException extends RuntimeException {
    public HydroException() {
        super("Hydro encountered an error");
    }

    public HydroException(String message) {
        super(message);
    }

    public HydroException(String message, Throwable cause) {
        super(message, cause);
    }

    public HydroException(Throwable cause) {
        super(cause);
    }
}
