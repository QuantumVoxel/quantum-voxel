package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;

public class BlockEntityTypes {
    public static final BlockEntityType<CrateBlockEntity> CRATE = register("crate", new BlockEntityType<>(CrateBlockEntity::new));
    public static final BlockEntityType<BlastFurnaceBlockEntity> BLAST_FURNACE = register("blast_furnace", new BlockEntityType<>(BlastFurnaceBlockEntity::new));

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
        Registries.BLOCK_ENTITY_TYPE.register(new NamespaceID(name), type);
        return type;
    }

    public static void init() {

    }
}
