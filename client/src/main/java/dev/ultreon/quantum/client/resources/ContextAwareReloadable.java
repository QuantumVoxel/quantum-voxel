package dev.ultreon.quantum.client.resources;

import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;

public interface ContextAwareReloadable {
    void reload(ResourceManager resourceManager, ReloadContext context);
}
