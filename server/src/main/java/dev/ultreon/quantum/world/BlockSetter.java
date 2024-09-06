package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.util.Point;

public interface BlockSetter {
    /**
     * Sets a block at the specified coordinates with the given block state.
     *
     * @param x     the x-coordinate of the block to set
     * @param y     the y-coordinate of the block to set
     * @param z     the z-coordinate of the block to set
     * @param block the BlockState to set at the specified coordinates
     * @return true if the block was successfully set, false otherwise
     */
    boolean set(int x, int y, int z, BlockState block);

    /**
     * Sets a block at the specified position with the given block state.
     *
     * @param pos the position of the block to set
     * @param block the BlockState to set at the specified position
     * @return true if the block was successfully set, false otherwise
     */
    default boolean set(Point pos, BlockState block) {
        return set(pos.getIntX(), pos.getIntY(), pos.getIntZ(), block);
    }
}
