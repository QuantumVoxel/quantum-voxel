package com.ultreon.quantum.world.gen.layer;

import com.google.common.base.Preconditions;
import com.ultreon.quantum.block.Block;
import com.ultreon.quantum.world.Chunk;
import com.ultreon.quantum.world.World;

public class GroundTerrainLayer extends TerrainLayer {
    private final Block block;
    private final int offset;
    private final int height;

    public GroundTerrainLayer(Block block, int offset, int height) {
        Preconditions.checkArgument(height > 0, "Height must be greater than zero");

        this.block = block;
        this.offset = offset;
        this.height = height;
    }

    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height) {
        if (y > height - this.offset - this.height && y <= height - this.offset) {
            chunk.set(x, y, z, this.block.createMeta());
            return true;
        }
        return false;
    }
}
