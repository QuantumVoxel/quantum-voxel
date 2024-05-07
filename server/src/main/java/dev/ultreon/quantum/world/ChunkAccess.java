package dev.ultreon.quantum.world;

import com.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.block.state.BlockProperties;

public interface ChunkAccess {

    boolean setFast(int x, int y, int z, BlockProperties block);

    boolean set(int x, int y, int z, BlockProperties block);

    BlockProperties getFast(int x, int y, int z);

    BlockProperties get(int x, int y, int z);

    Vec3i getOffset();

    int getHighest(int x, int z);
}
