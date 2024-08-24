package dev.ultreon.quantum.api.events.server;

import dev.ultreon.quantum.server.QuantumServer;

public class ServerStartingEvent extends ServerEvent {
    public ServerStartingEvent(QuantumServer server) {
        super(server);
    }
}
