package com.ultreon.quantum.server.dedicated;

import com.ultreon.quantum.CommonLoader;
import com.ultreon.quantum.block.Blocks;
import com.ultreon.quantum.entity.EntityTypes;
import com.ultreon.quantum.item.Items;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.server.GameCommands;
import com.ultreon.quantum.world.gen.biome.Biomes;
import com.ultreon.quantum.world.gen.noise.NoiseConfigs;
import net.fabricmc.loader.api.FabricLoader;

/**
 * This class is responsible for loading the server configurations and initializing various components.
 */
public class ServerLoader {

    /**
     * Loads server configurations and initializes various components.
     */
    public void load() {
        // Initialize configuration entry points
        CommonLoader.initConfigEntrypoints(FabricLoader.getInstance());

        // Initialize registries
        Registries.nopInit();
        Blocks.nopInit();
        Items.nopInit();
        NoiseConfigs.nopInit();
        EntityTypes.nopInit();
        Biomes.nopInit();

        // Register game commands
        GameCommands.register();
    }
}
