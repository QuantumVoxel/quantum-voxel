package dev.ultreon.quantum.client.resources;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.NamespaceID;

/**
 * Interface for loadable resources.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public interface LoadableResource {
    /**
     * Loads the resource.
     * 
     * @param client The client.
     */
    void load(QuantumClient client);

    /**
     * Gets the resource ID.
     * 
     * @return The resource ID.
     */
    NamespaceID resourceId();
}