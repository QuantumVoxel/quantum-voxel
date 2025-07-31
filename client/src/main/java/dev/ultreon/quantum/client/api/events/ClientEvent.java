package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.client.QuantumClient;

public interface ClientEvent extends Event {
    default QuantumClient getClient() {
        return QuantumClient.get();
    }
}
