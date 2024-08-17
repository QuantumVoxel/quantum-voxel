package dev.ultreon.quantum.api.events.server;

import dev.ultreon.quantum.server.QuantumServer;

public class ServerStoppedEvent extends ServerEvent {
    public ServerStoppedEvent(QuantumServer server) {
        super(server);
    }
}
