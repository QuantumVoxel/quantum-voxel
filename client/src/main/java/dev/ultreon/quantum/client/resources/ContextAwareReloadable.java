package dev.ultreon.quantum.client.resources;

import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;

/**
 * Interface for reloadable resources that require a context.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public interface ContextAwareReloadable {
    /**
     * Reloads the resource.
     * 
     * @param resourceManager The resource manager.
     * @param context The reload context.
     */
    void reload(ResourceManager resourceManager, ReloadContext context);
}
