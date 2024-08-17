package dev.ultreon.quantum.api.events.server;

import dev.ultreon.quantum.server.QuantumServer;

public class ServerStartedEvent extends ServerEvent {
    public ServerStartedEvent(QuantumServer server) {
        super(server);
    }
}
