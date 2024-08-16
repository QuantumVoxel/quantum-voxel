package dev.ultreon.quantum.world;

import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.world.vec.BlockVec;

public interface ChunkReader {
    BlockProperties getFast(int x, int y, int z);

    BlockProperties get(int x, int y, int z);

    Vec3i getOffset();

    int getHeight(int x, int z);

    default BlockProperties get(BlockVec localize) {
        return get(localize.getIntX(), localize.getIntY(), localize.getIntZ());
    }

    default BlockProperties get(Vec3i cpy) {
        return get(cpy.x, cpy.y, cpy.z);
    }

    default BlockProperties getFast(BlockVec localize) {
        return getFast(localize.getIntX(), localize.getIntY(), localize.getIntZ());
    }

    default BlockProperties getFast(Vec3i cpy) {
        return getFast(cpy.x, cpy.y, cpy.z);
    }

    boolean isDisposed();
}
