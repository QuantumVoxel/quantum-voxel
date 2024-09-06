package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.util.Point;
import dev.ultreon.quantum.util.Vec3i;

public interface ChunkReader {
    /**
     * Retrieves the block state at the specified coordinates.
     *
     * @param x the x-coordinate of the block's location
     * @param y the y-coordinate of the block's location
     * @param z the z-coordinate of the block's location
     * @return the block state at the specified coordinates
     */
    BlockState get(int x, int y, int z);

    Vec3i getOffset();

    int getHeight(int x, int z);

    /**
     * Retrieves the BlockState at the specified BlockVec position.
     *
     * @param vec the vector object containing the coordinates.
     * @return the block state at the specified coordinates.
     */
    default BlockState get(Point vec) {
        return get(vec.getIntX(), vec.getIntY(), vec.getIntZ());
    }

    boolean isDisposed();

    WorldReader getWorld();
}
