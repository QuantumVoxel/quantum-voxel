package com.ultreon.quantum.block.entity;

import com.ultreon.quantum.block.Block;
import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.BlockPos;
import com.ultreon.quantum.world.World;
import com.ultreon.data.types.MapType;

import java.util.Objects;

public abstract class BlockEntity {
    private final BlockEntityType<?> type;
    protected final World world;
    protected final BlockPos pos;

    public BlockEntity(BlockEntityType<?> type, World world, BlockPos pos) {
        this.type = type;
        this.world = world;
        this.pos = pos;
    }

    public Block getBlock() {
        return world.get(pos).getBlock();
    }

    public BlockProperties getBlockMeta() {
        return world.get(pos);
    }

    public World getWorld() {
        return world;
    }

    public BlockPos pos() {
        return pos;
    }

    public BlockEntityType<?> getType() {
        return type;
    }

    public static BlockEntity fullyLoad(World world, BlockPos pos, MapType mapType) {
        Identifier type = Identifier.tryParse(mapType.getString("type"));
        BlockEntityType<?> value = Registries.BLOCK_ENTITY_TYPE.get(type);
        return value.load(world, pos, mapType);
    }

    public void load(MapType data) {

    }

    public MapType save(MapType data) {
        data.putString("type", Objects.requireNonNull(type.getId()).toString());
        data.putInt("x", pos.x());
        data.putInt("y", pos.y());
        data.putInt("z", pos.z());
        return data;
    }
}
