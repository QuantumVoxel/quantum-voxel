package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ModIconOverrideRegistry {
    private static final Map<String, Identifier> OVERRIDES = new HashMap<>();

    public static Identifier get(String modId) {
        return ModIconOverrideRegistry.OVERRIDES.get(modId);
    }

    public static void set(String modId, Identifier iconId) {
        ModIconOverrideRegistry.OVERRIDES.put(modId, iconId);
    }
}
