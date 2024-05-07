package dev.ultreon.quantum.world.gen.layer;

import com.google.common.base.Preconditions;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.rng.RNG;

public class GroundTerrainLayer extends TerrainLayer {
    private final Block block;
    private final int offset;
    private final int height;

    public GroundTerrainLayer(Block block, int offset, int height) {
        Preconditions.checkArgument(height > 0, "Height must be greater than zero");

        this.block = block;
        this.offset = offset;
        this.height = height;
    }

    @Override
    public boolean handle(World world, Chunk chunk, RNG rng, int x, int y, int z, int height) {
        if (y > height - this.offset - this.height && y <= height - this.offset) {
            chunk.set(x, y, z, this.block.createMeta());
            return true;
        }
        return false;
    }
}
