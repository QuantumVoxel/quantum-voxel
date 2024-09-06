package dev.ultreon.quantum.world.gen.layer;

import dev.ultreon.quantum.world.BlockSetter;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.rng.RNG;

public class AirTerrainLayer extends TerrainLayer {
    @Override
    public boolean handle(World world, BlockSetter chunk, RNG rng, int x, int y, int z, int height) {
        return false;
    }
}
