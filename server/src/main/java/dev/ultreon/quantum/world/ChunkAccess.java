package dev.ultreon.quantum.world;

import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.block.state.BlockProperties;

public interface ChunkAccess extends ChunkReader {
    default void setFast(Vec3i pos, BlockProperties block) {
        setFast(pos.x, pos.y, pos.z, block);
    }

    default void setFast(BlockPos pos, BlockProperties block) {
        setFast(pos.x(), pos.y(), pos.z(), block);
    }

    boolean setFast(int x, int y, int z, BlockProperties block);

    default void set(Vec3i pos, BlockProperties block) {
        set(pos.x, pos.y, pos.z, block);
    }

    default void set(BlockPos pos, BlockProperties block) {
        set(pos.x(), pos.y(), pos.z(), block);
    }

    boolean set(int x, int y, int z, BlockProperties block);
}
