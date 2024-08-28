package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.vec.BlockVec;

public interface ChunkReader {
    BlockState get(int x, int y, int z);

    Vec3i getOffset();

    int getHeight(int x, int z);

    default BlockState get(BlockVec localize) {
        return get(localize.getIntX(), localize.getIntY(), localize.getIntZ());
    }

    default BlockState get(Vec3i cpy) {
        return get(cpy.x, cpy.y, cpy.z);
    }

    boolean isDisposed();
}
