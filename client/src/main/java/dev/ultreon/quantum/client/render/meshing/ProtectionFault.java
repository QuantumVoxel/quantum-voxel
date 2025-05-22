package dev.ultreon.quantum.client.render.meshing;

public class ProtectionFault extends Error {
    public ProtectionFault() {
    }

    public ProtectionFault(String message) {
        super(message);
    }

    public ProtectionFault(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtectionFault(Throwable cause) {
        super(cause);
    }
}
