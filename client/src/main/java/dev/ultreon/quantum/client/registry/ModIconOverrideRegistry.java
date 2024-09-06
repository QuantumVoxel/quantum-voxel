package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.util.NamespaceID;

import java.util.HashMap;
import java.util.Map;

public class ModIconOverrideRegistry {
    private static final Map<String, NamespaceID> OVERRIDES = new HashMap<>();

    public static NamespaceID get(String modId) {
        return ModIconOverrideRegistry.OVERRIDES.get(modId);
    }

    public static void set(String modId, NamespaceID iconId) {
        ModIconOverrideRegistry.OVERRIDES.put(modId, iconId);
    }
}
