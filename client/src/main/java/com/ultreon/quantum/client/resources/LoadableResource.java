package com.ultreon.quantum.client.resources;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.util.Identifier;

public interface LoadableResource {
    void load(QuantumClient client);

    Identifier resourceId();

}
