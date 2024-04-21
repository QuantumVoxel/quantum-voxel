package com.ultreon.quantum.world.gen.layer;

import com.ultreon.quantum.block.Blocks;
import com.ultreon.quantum.world.Chunk;
import com.ultreon.quantum.world.World;

public class AirTerrainLayer extends TerrainLayer {
    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height) {
        return false;
    }
}
