package dev.ultreon.quantum.util;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.Mod;

public class ModLoadingContext {
    private static ModLoadingContext instance;
    private final Mod mod;

    private ModLoadingContext(Mod mod) {
        this.mod = mod;
    }

    public static ModLoadingContext get() {
        return instance;
    }

    public static void withinContext(Mod mod, Runnable runnable) {
        instance = new ModLoadingContext(mod);
        try {
            runnable.run();
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to load mod " + mod.getId(), e);
            throw new RuntimeException(e);
        } finally {
            instance = null;
        }
    }

    public Mod getMod() {
        return mod;
    }
}
