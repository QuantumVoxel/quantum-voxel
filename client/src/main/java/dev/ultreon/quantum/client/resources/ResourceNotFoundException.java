package dev.ultreon.quantum.client.resources;

import dev.ultreon.quantum.util.NamespaceID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(NamespaceID id) {
        super(id.toString());
    }
}
