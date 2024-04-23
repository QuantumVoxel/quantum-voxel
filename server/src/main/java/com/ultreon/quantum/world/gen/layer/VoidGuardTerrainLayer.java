package com.ultreon.quantum.world.gen.layer;

import com.ultreon.quantum.block.Blocks;
import com.ultreon.quantum.world.Chunk;
import com.ultreon.quantum.world.World;
import com.ultreon.quantum.world.rng.RNG;

public class VoidGuardTerrainLayer extends TerrainLayer {
    public VoidGuardTerrainLayer() {

    }

    @Override
    public boolean handle(World world, Chunk chunk, RNG rng, int x, int y, int z, int height) {
        if (y == 70) {
            chunk.set(x, y, z, Blocks.VOIDGUARD.createMeta());
            return true;
        }
        return false;
    }
}
