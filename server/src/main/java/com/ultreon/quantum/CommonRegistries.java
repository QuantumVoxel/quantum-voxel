package com.ultreon.quantum;

import com.ultreon.quantum.block.Blocks;
import com.ultreon.quantum.block.entity.BlockEntityTypes;
import com.ultreon.quantum.entity.EntityTypes;
import com.ultreon.quantum.item.Items;
import com.ultreon.quantum.server.GameCommands;
import com.ultreon.quantum.sound.event.SoundEvents;
import com.ultreon.quantum.world.gen.noise.NoiseConfigs;

public class CommonRegistries {
    public static void register() {
        Blocks.init();
        BlockEntityTypes.init();
        Items.init();
        NoiseConfigs.init();
        EntityTypes.init();
        SoundEvents.init();

        GameCommands.register();
    }
}
