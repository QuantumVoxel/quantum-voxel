package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.Nullable;

public class BlockEntityType<T extends BlockEntity> {
    private final BlockEntityFactory<T> factory;

    public BlockEntityType(BlockEntityFactory<T> factory) {
        this.factory = factory;
    }

    public T create(World world, BlockVec pos) {
        return factory.create(this, world, pos);
    }

    public T load(World world, BlockVec pos, MapType data) {
        T blockEntity = factory.create(this, world, pos);
        blockEntity.load(data);
        return blockEntity;
    }

    public @Nullable NamespaceID getId() {
        return Registries.BLOCK_ENTITY_TYPE.getId(this);
    }

    public int getRawId() {
        return Registries.BLOCK_ENTITY_TYPE.getRawId(this);
    }

    @FunctionalInterface
    public interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockEntityType<T> type, World world, BlockVec pos);
    }
}
