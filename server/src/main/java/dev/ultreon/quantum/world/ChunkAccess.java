package dev.ultreon.quantum.world;

import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.world.vec.BlockVec;

public interface ChunkAccess extends ChunkReader {
    default void setFast(Vec3i pos, BlockProperties block) {
        setFast(pos.x, pos.y, pos.z, block);
    }

    default void setFast(BlockVec pos, BlockProperties block) {
        setFast(pos.getIntX(), pos.getIntY(), pos.getIntZ(), block);
    }

    boolean setFast(int x, int y, int z, BlockProperties block);

    default void set(Vec3i pos, BlockProperties block) {
        set(pos.x, pos.y, pos.z, block);
    }

    default void set(BlockVec pos, BlockProperties block) {
        set(pos.getIntX(), pos.getIntY(), pos.getIntZ(), block);
    }

    boolean set(int x, int y, int z, BlockProperties block);
}
