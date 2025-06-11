package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.world.vec.BlockVec;

public final class BlockCollision implements Collision {
    private final BlockState block;
    private final BlockVec vec;
    private final World world;

    public BlockCollision(BlockState block, BlockVec vec, World world) {
        this.block = block;
        this.vec = vec;
        this.world = world;
    }

    public BlockState getBlock() {
        return block;
    }

    @Override
    public World getWorld() {
        return world;
    }
}
