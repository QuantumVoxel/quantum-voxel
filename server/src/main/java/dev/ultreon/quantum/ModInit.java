package dev.ultreon.quantum;

import net.fabricmc.api.ModInitializer;

/**
 * This interface represents a modification initializer.
 */
public interface ModInit extends ModInitializer {
    /**
     * Key for the entry point.
     */
    String ENTRYPOINT_KEY = "init";

    /**
     * Method called during initialization.
     */
    void onInitialize();
}
