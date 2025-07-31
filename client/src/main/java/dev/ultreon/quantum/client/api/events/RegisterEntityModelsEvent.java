package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;

public class RegisterEntityModelsEvent implements ClientEvent, ClientReloadEvent {
    private final ResourceManager resourceManager;
    private final ReloadContext reloadContext;

    public RegisterEntityModelsEvent(ResourceManager resourceManager, ReloadContext reloadContext) {
        this.resourceManager = resourceManager;
        this.reloadContext = reloadContext;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public ReloadContext getReloadContext() {
        return reloadContext;
    }
}
