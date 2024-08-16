package dev.ultreon.quantum.world;

import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.block.state.BlockProperties;

public interface ChunkReader {
    BlockProperties getFast(int x, int y, int z);

    BlockProperties get(int x, int y, int z);

    Vec3i getOffset();

    int getHighest(int x, int z);

    default BlockProperties get(BlockVec localize) {
        return get(localize.x(), localize.y(), localize.z());
    }

    default BlockProperties get(Vec3i cpy) {
        return get(cpy.x, cpy.y, cpy.z);
    }

    default BlockProperties getFast(BlockVec localize) {
        return getFast(localize.x(), localize.y(), localize.z());
    }

    default BlockProperties getFast(Vec3i cpy) {
        return getFast(cpy.x, cpy.y, cpy.z);
    }

    boolean isDisposed();
}
