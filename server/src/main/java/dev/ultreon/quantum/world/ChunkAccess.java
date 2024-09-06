package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.util.Point;

public interface ChunkAccess extends ChunkReader {
    /**
     * Sets the block at the specified position with the provided block state.
     *
     * @param pos   the position of the block to be set, encapsulated in a BlockVec object
     * @param block the new BlockState to be assigned to the block at the specified position
     * @return
     */
    default boolean set(Point pos, BlockState block) {
        set(pos.getIntX(), pos.getIntY(), pos.getIntZ(), block);
        return false;
    }

    /**
     * Sets the block state at the specified coordinates.
     *
     * @param x the x-coordinate of the block
     * @param y the y-coordinate of the block
     * @param z the z-coordinate of the block
     * @param block the new block state to set
     * @return true if the block state was successfully set, false otherwise
     */
    boolean set(int x, int y, int z, BlockState block);

    @Override
    WorldAccess getWorld();
}
