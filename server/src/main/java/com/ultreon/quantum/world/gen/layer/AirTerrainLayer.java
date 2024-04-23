package com.ultreon.quantum.world.gen.layer;

import com.ultreon.quantum.world.Chunk;
import com.ultreon.quantum.world.World;
import com.ultreon.quantum.world.rng.RNG;

public class AirTerrainLayer extends TerrainLayer {
    @Override
    public boolean handle(World world, Chunk chunk, RNG rng, int x, int y, int z, int height) {
        return false;
    }
}
