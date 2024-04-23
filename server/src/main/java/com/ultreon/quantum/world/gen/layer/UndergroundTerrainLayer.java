package com.ultreon.quantum.world.gen.layer;

import com.ultreon.quantum.block.Block;
import com.ultreon.quantum.world.Chunk;
import com.ultreon.quantum.world.World;
import com.ultreon.quantum.world.rng.RNG;

public class UndergroundTerrainLayer extends TerrainLayer {
    private final Block block;
    private final int offset;

    public UndergroundTerrainLayer(Block block, int offset) {
        this.block = block;
        this.offset = offset;
    }

    @Override
    public boolean handle(World world, Chunk chunk, RNG rng, int x, int y, int z, int height) {
        if (y <= height - offset) {
            chunk.set(x, y, z, block.createMeta());
            return true;
        }
        return false;
    }
}
