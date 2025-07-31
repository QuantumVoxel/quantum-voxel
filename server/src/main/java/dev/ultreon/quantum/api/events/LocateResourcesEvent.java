package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.resources.ResourceManager;

public interface LocateResourcesEvent extends Event {
    ResourceManager getResourceManager();

    class Server implements LocateResourcesEvent {
        private final ResourceManager resourceManager;

        public Server(ResourceManager resourceManager) {
            this.resourceManager = resourceManager;
        }

        @Override
        public ResourceManager getResourceManager() {
            return resourceManager;
        }
    }
}
