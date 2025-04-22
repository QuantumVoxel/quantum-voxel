package dev.ultreon.quantum.world.gen.layer;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.world.BlockSetter;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.rng.RNG;

public class GroundTerrainLayer extends TerrainLayer {
    public final Block block;
    private final int offset;
    private final int height;

    public GroundTerrainLayer(Block block, int offset, int height) {
        this.block = block;
        this.offset = offset;
        this.height = height;
    }

    @Override
    public boolean handle(World world, BlockSetter chunk, RNG rng, int x, int y, int z, int height) {
        if (y > height - this.offset - this.height && y <= height - this.offset) {
            chunk.set(x, y, z, this.block.getDefaultState());
            return true;
        }
        return false;
    }
}
