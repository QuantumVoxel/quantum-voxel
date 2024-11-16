package dev.ultreon.quantum;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.entity.BlockEntityTypes;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.item.Items;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.server.GameCommands;
import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.world.capability.Capabilities;
import dev.ultreon.quantum.world.container.FuelRegistry;
import dev.ultreon.quantum.world.particles.ParticleTypes;

public class CommonRegistries {
    public static void register() {
        Blocks.init();
        BlockEntityTypes.init();
        Items.init();
        EntityTypes.init();
        SoundEvents.init();
        ParticleTypes.init();
        Capabilities.init();

        RecipeType.nopInit();

        registerFuels();

        GameCommands.register();
    }

    private static void registerFuels() {
        FuelRegistry.register(Items.CRATE, 300);
        FuelRegistry.register(Items.LOG, 300);
        FuelRegistry.register(Items.PLANKS, 200);
        FuelRegistry.register(Items.PLANK, 100);
        FuelRegistry.register(Items.STICK, 50);
        FuelRegistry.register(Items.WOODEN_AXE, 150);
        FuelRegistry.register(Items.WOODEN_SHOVEL, 150);
        FuelRegistry.register(Items.WOODEN_PICKAXE, 150);
    }
}
