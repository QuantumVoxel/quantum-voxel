package dev.ultreon.quantum.world.gen.layer;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.rng.RNG;

public class StoneyPeaksTerrainLayer extends TerrainLayer {
    private final Block stone;
    private final int height;

    public StoneyPeaksTerrainLayer(Block stone, int height) {
        this.stone = stone;
        this.height = height;
    }

    @Override
    public boolean handle(World world, Chunk chunk, RNG rng, int x, int y, int z, int height) {
        if (y >= this.height && y <= this.height + 4 && rng.chance(y - this.height + 1)) {
            chunk.set(x, y, z, this.stone.createMeta());
            return true;
        }
        return false;
    }
}
