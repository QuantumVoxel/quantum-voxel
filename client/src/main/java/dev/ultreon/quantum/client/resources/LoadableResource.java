package dev.ultreon.quantum.client.resources;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.NamespaceID;

public interface LoadableResource {
    void load(QuantumClient client);

    NamespaceID resourceId();

}
