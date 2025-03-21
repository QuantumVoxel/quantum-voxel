package dev.ultreon.quantum.client.resources;

import dev.ultreon.quantum.util.NamespaceID;

/**
 * Exception thrown when a resource is not found.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Constructs a new ResourceNotFoundException with the given namespace ID.
     * 
     * @param id The namespace ID of the resource.
     */
    public ResourceNotFoundException(NamespaceID id) {
        super(id.toString());
    }
}
