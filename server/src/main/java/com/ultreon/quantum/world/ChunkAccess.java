package com.ultreon.quantum.world;

import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public interface ChunkAccess {

    boolean setFast(int x, int y, int z, BlockProperties block);

    boolean set(int x, int y, int z, BlockProperties block);

    BlockProperties getFast(int x, int y, int z);

    BlockProperties get(int x, int y, int z);

    Vec3i getOffset();

    int getHighest(int x, int z);
}
