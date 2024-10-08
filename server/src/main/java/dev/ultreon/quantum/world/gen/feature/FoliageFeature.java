package dev.ultreon.quantum.world.gen.feature;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.world.Fork;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.gen.TerrainFeature;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FoliageFeature extends TerrainFeature {
    private final NoiseConfig noiseConfig;
    private final Block material;
    private final float threshold;
    private final Random random = new Random();

    public FoliageFeature(NoiseConfig trees, Block material, float threshold) {
        super();

        this.noiseConfig = trees;
        this.material = material;
        this.threshold = threshold;
    }

    @Override
    public boolean handle(@NotNull Fork setter, long seed, int x, int y, int z) {
        if (this.noiseConfig == null) return false;

        this.random.setSeed(seed);

        if (this.random.nextFloat() < this.threshold) {
            for (int xOffset = -1; xOffset < 1; xOffset++) {
                for (int zOffset = -1; zOffset < 1; zOffset++) {
                    setter.set(x + xOffset, y, z + zOffset, this.material.getDefaultState());
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public void create(@NotNull ServerWorld world) {

    }
}
