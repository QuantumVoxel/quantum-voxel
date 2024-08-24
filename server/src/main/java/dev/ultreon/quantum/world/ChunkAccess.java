package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.vec.BlockVec;

public interface ChunkAccess extends ChunkReader {
    default void setFast(Vec3i pos, BlockState block) {
        setFast(pos.x, pos.y, pos.z, block);
    }

    default void setFast(BlockVec pos, BlockState block) {
        setFast(pos.getIntX(), pos.getIntY(), pos.getIntZ(), block);
    }

    boolean setFast(int x, int y, int z, BlockState block);

    default void set(BlockVec pos, BlockState block) {
        set(pos.getIntX(), pos.getIntY(), pos.getIntZ(), block);
    }

    boolean set(int x, int y, int z, BlockState block);
}
