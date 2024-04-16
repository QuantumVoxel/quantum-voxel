package com.ultreon.craft.network;

import java.io.IOException;

public class PacketIntegrityException extends IOException {
    public PacketIntegrityException() {
        super();
    }

    public PacketIntegrityException(String message) {
        super(message);
    }

    public PacketIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }

    public PacketIntegrityException(Throwable cause) {
        super(cause);
    }
}
