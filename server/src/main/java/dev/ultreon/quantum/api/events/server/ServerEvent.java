package dev.ultreon.quantum.api.events.server;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.server.QuantumServer;

public abstract class ServerEvent extends Event {
    private final QuantumServer server;

    public ServerEvent(QuantumServer server) {
        this.server = server;
    }

    public QuantumServer getServer() {
        return server;
    }
}
