package dev.ultreon.quantum.server.dedicated;

import dev.ultreon.quantum.CommonLoader;
import dev.ultreon.quantum.CommonRegistries;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.api.neocommand.CommandRegistration;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.server.GameCommands;

/**
 * This class is responsible for loading the server configurations and initializing various components.
 */
public class ServerLoader {

    /**
     * Loads server configurations and initializes various components.
     */
    public void load() {
        // Initialize configuration entry points
        CommonLoader.initConfigEntrypoints(GamePlatform.get());

        // Initialize registries
        Registries.nopInit();
        CommonRegistries.register();

        // Register game commands
        CommandRegistration.register();
    }
}
