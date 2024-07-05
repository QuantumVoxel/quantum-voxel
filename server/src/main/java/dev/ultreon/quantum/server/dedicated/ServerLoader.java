package dev.ultreon.quantum.server.dedicated;

import dev.ultreon.quantum.CommonLoader;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.item.Items;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.server.GameCommands;
import dev.ultreon.quantum.text.icon.EmoteMap;
import dev.ultreon.quantum.text.icon.IconMap;
import dev.ultreon.quantum.world.gen.biome.Biomes;
import dev.ultreon.quantum.world.gen.noise.NoiseConfigs;

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
        Blocks.init();
        Items.init();
        NoiseConfigs.init();
        EntityTypes.init();
        Biomes.init();

        // Register game commands
        GameCommands.register();
    }
}
