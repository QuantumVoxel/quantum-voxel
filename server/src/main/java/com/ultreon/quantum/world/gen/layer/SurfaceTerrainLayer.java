package com.ultreon.quantum.world.gen.layer;

import com.ultreon.quantum.block.Block;
import com.ultreon.quantum.world.Chunk;
import com.ultreon.quantum.world.World;

public class SurfaceTerrainLayer extends TerrainLayer {
    private final Block surfaceBlock;
    private final int height;

    public SurfaceTerrainLayer(Block surfaceBlock, int height) {
        this.surfaceBlock = surfaceBlock;
        this.height = height;
    }

    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height) {
        if (y >= height - this.height && y <= height) {
            chunk.set(x, y, z, this.surfaceBlock.createMeta());
            return true;
        }
        return false;
    }
}
