package dev.ultreon.quantum.world.gen.feature;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.debug.WorldGenDebugContext;
import dev.ultreon.quantum.world.Fork;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.gen.TerrainFeature;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.quantum.world.rng.RNG;
import org.jetbrains.annotations.NotNull;

public class TreeFeature extends TerrainFeature {
    private final NoiseConfig noiseConfig;
    private final Block trunk;
    private final Block leaves;
    private final float threshold;
    private final RNG random = new JavaRNG();
    private final int minTrunkHeight;
    private final int maxTrunkHeight;

    public TreeFeature(NoiseConfig trees, Block trunk, Block leaves, float threshold, int minTrunkHeight, int maxTrunkHeight) {
        super();

        this.noiseConfig = trees;
        this.trunk = trunk;
        this.leaves = leaves;
        this.threshold = threshold;
        this.minTrunkHeight = minTrunkHeight;
        this.maxTrunkHeight = maxTrunkHeight;
    }

    @Override
    public boolean shouldPlace(int x, int y, int z, @NotNull BlockState origin) {
        return origin.is(Blocks.GRASS_BLOCK) || origin.is(Blocks.SNOWY_GRASS_BLOCK) || origin.is(Blocks.DIRT);
    }

    @Override
    public boolean handle(@NotNull Fork setter, long seed, int x, int y, int z) {
        if (this.noiseConfig == null) return false;

        y++;

        this.random.setSeed(seed);
        this.random.setSeed(this.random.nextLong());

        if (this.random.nextFloat() < this.threshold) {
            if (WorldGenDebugContext.isActive()) {
                System.out.println("[Start " + Thread.currentThread().getId() + "] TreeFeature: " + x + ", " + z + ", " + y);
            }

            var trunkHeight = this.random.nextInt(this.minTrunkHeight, this.maxTrunkHeight);

            for (int ty = y; ty < y + trunkHeight; ty++) {
                setter.set(x, ty, z, this.trunk.getDefaultState());
            }

            setter.set(x, y - 1, z, Blocks.DIRT.getDefaultState());
            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    for (int ty = trunkHeight - 1; ty <= trunkHeight + 1; ty++) {
                        if (xOffset == 0 && zOffset == 0 && ty != trunkHeight + 1) continue;
                        setter.set(x + xOffset, ty, z + zOffset, this.leaves.getDefaultState());

                        if (WorldGenDebugContext.isActive()) {
                            System.out.println("[End " + Thread.currentThread().getId() + "] TreeFeature: " + x + ", " + z + ", " + y + " - Setting leaf at " + (x + xOffset) + ", " + ty + ", " + (z + zOffset));
                        }
                    }
                }
            }

            if (WorldGenDebugContext.isActive()) {
                System.out.println("[End " + Thread.currentThread().getId() + "] TreeFeature: " + x + ", " + z + ", " + y + " - Success");
            }
            return true;
        }

        return false;
    }

    @Override
    public void create(@NotNull ServerWorld world) {

    }
}
