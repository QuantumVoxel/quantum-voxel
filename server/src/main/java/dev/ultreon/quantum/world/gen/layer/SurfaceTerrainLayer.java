package dev.ultreon.quantum.world.gen.layer;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.world.BlockSetter;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.rng.RNG;

public class SurfaceTerrainLayer extends TerrainLayer {
    public final Block surfaceBlock;
    private final int height;

    public SurfaceTerrainLayer(Block surfaceBlock, int height) {
        this.surfaceBlock = surfaceBlock;
        this.height = height;
    }

    @Override
    public boolean handle(World world, BlockSetter chunk, RNG rng, int x, int y, int z, int height) {
        if (y >= height - this.height && y <= height) {
            chunk.set(x, y, z, this.surfaceBlock.getDefaultState());
            return true;
        }
        return false;
    }
}
