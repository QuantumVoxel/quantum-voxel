package dev.ultreon.quantum.client.resources;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.Identifier;

public interface LoadableResource {
    void load(QuantumClient client);

    Identifier resourceId();

}
