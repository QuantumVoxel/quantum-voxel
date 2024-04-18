package com.ultreon.quantum.client.resources;

import com.ultreon.quantum.resources.ReloadContext;
import com.ultreon.quantum.resources.ResourceManager;

public interface ContextAwareReloadable {
    void reload(ResourceManager resourceManager, ReloadContext context);
}
