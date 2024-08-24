package dev.ultreon.quantum.api.events.server;

import dev.ultreon.quantum.server.QuantumServer;

public class ServerStoppingEvent extends ServerEvent {
    public ServerStoppingEvent(QuantumServer server) {
        super(server);
    }
}
