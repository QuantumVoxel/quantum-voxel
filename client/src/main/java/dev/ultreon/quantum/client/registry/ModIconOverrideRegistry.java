package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.util.NamespaceID;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the registry of mod icon overrides.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class ModIconOverrideRegistry {
    private static final Map<String, NamespaceID> OVERRIDES = new HashMap<>();

    /**
     * Gets the icon location for a mod.
     *
     * @param modId the mod id.
     * @return the icon location.
     */
    public static NamespaceID get(String modId) {
        return ModIconOverrideRegistry.OVERRIDES.get(modId);
    }

    /**
     * Sets the icon location for a mod.
     *
     * @param modId the mod id.
     * @param iconId the icon location.
     */
    public static void set(String modId, NamespaceID iconId) {
        ModIconOverrideRegistry.OVERRIDES.put(modId, iconId);
    }
}
