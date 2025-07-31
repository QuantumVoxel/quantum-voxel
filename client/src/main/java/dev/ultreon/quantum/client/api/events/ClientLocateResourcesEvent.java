package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.api.events.LocateResourcesEvent;
import dev.ultreon.quantum.resources.ResourceManager;

public class ClientLocateResourcesEvent implements ClientEvent, LocateResourcesEvent {
    private final ResourceManager resourceManager;

    public ClientLocateResourcesEvent(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
