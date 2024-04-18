package com.ultreon.quantum.client.resources;

import com.ultreon.quantum.util.Identifier;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(Identifier id) {
        super(id.toString());
    }
}
