package dev.ultreon.quantum.world.gen.layer;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.rng.RNG;

public class SurfaceTerrainLayer extends TerrainLayer {
    private final Block surfaceBlock;
    private final int height;

    public SurfaceTerrainLayer(Block surfaceBlock, int height) {
        this.surfaceBlock = surfaceBlock;
        this.height = height;
    }

    @Override
    public boolean handle(World world, Chunk chunk, RNG rng, int x, int y, int z, int height) {
        if (y >= height - this.height && y <= height) {
            chunk.set(x, y, z, this.surfaceBlock.createMeta());
            return true;
        }
        return false;
    }
}
