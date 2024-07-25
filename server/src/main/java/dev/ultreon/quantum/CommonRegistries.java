package dev.ultreon.quantum;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.entity.BlockEntityTypes;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.item.Items;
import dev.ultreon.quantum.server.GameCommands;
import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.world.capability.Capabilities;
import dev.ultreon.quantum.world.gen.noise.NoiseConfigs;
import dev.ultreon.quantum.world.particles.ParticleTypes;

public class CommonRegistries {
    public static void register() {
        Blocks.init();
        BlockEntityTypes.init();
        Items.init();
        NoiseConfigs.init();
        EntityTypes.init();
        SoundEvents.init();
        ParticleTypes.init();
        Capabilities.init();

        GameCommands.register();
    }
}
