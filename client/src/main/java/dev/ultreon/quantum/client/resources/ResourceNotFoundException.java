package dev.ultreon.quantum.client.resources;

import dev.ultreon.quantum.util.Identifier;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(Identifier id) {
        super(id.toString());
    }
}
