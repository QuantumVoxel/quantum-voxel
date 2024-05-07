package dev.ultreon.quantum.world.gen.layer;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.rng.RNG;

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
