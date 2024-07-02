package dev.ultreon.quantum.world;

import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.block.state.BlockData;

public interface ChunkAccess {

    boolean setFast(int x, int y, int z, BlockData block);

    boolean set(int x, int y, int z, BlockData block);

    BlockData getFast(int x, int y, int z);

    BlockData get(int x, int y, int z);

    Vec3i getOffset();

    int getHighest(int x, int z);
}
