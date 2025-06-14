package dev.ultreon.quantum.world.gen.layer;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.world.BlockSetter;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.rng.RNG;

public class WaterTerrainLayer extends TerrainLayer {
    private final int waterLevel;

    public WaterTerrainLayer() {
        this(World.SEA_LEVEL);
    }

    public WaterTerrainLayer(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    @Override
    public boolean handle(World world, BlockSetter chunk, RNG rng, int x, int y, int z, int height) {
        // Set water layer from height up to water level y
        if (y <= this.waterLevel + 1 && y > height) {
            return false;
        }

        // Set sand layer from the height - 3 up to water level + 2
        if (y <= this.waterLevel + 2 && y <= height && y >= height - 3 && height <= this.waterLevel + 2) {
            chunk.set(x, y, z, Blocks.SAND.getDefaultState());
            return true;
        }

        return false;

    }
}
