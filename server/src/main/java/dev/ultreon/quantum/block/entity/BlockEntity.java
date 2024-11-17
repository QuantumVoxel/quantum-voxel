package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.capability.CapabilityType;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.ubo.types.MapType;
import lombok.Getter;

import java.util.Objects;

public abstract class BlockEntity implements CapabilityHolder {
    @Getter
    private final BlockEntityType<?> type;
    @Getter
    protected final World world;
    protected final BlockVec pos;

    public BlockEntity(BlockEntityType<?> type, World world, BlockVec pos) {
        this.type = type;
        this.world = world;
        this.pos = pos;
    }

    public Block getBlock() {
        return world.get(pos).getBlock();
    }

    public BlockState getBlockMeta() {
        return world.get(pos);
    }

    public BlockVec pos() {
        return pos;
    }

    public static BlockEntity fullyLoad(World world, BlockVec pos, MapType mapType) {
        NamespaceID type = NamespaceID.tryParse(mapType.getString("type"));
        BlockEntityType<?> value = Registries.BLOCK_ENTITY_TYPE.get(type);
        return value.load(world, pos, mapType);
    }

    public void load(MapType data) {

    }

    public MapType save(MapType data) {
        data.putString("type", Objects.requireNonNull(type.getId()).toString());
        data.putInt("x", pos.getIntX());
        data.putInt("y", pos.getIntY());
        data.putInt("z", pos.getIntZ());
        return data;
    }

    public void tick() {
        // For override
    }


    public int getCapacity(CapabilityType<?, ?> capability) {
        return 0;
    }

    public void onUpdate(MapType data) {

    }

    public BlockEntityType<?> getType() {
        return type;
    }
}
