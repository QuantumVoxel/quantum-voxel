package dev.ultreon.quantum.world.gen.feature;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.debug.WorldGenDebugContext;
import dev.ultreon.quantum.world.Fork;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.gen.TerrainFeature;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.quantum.world.rng.RNG;
import org.jetbrains.annotations.NotNull;

public class CactiFeature extends TerrainFeature {
    private final NoiseConfig noiseConfig;
    private final Block block;
    private final float threshold;
    private final RNG random = new JavaRNG();
    private final int minTrunkHeight;
    private final int maxTrunkHeight;

    public CactiFeature(NoiseConfig trees, Block block, float threshold, int minTrunkHeight, int maxTrunkHeight) {
        super();

        this.noiseConfig = trees;
        this.block = block;
        this.threshold = threshold;
        this.minTrunkHeight = minTrunkHeight;
        this.maxTrunkHeight = maxTrunkHeight;
    }

    @Override
    public boolean handle(@NotNull Fork setter, long seed, int x, int y, int z) {
        if (this.noiseConfig == null) return false;

        this.random.setSeed(seed);
        this.random.setSeed(this.random.nextLong());

        if (this.random.nextFloat() < this.threshold) {
            if (WorldGenDebugContext.isActive()) {
                System.out.println("[Start " + Thread.currentThread().threadId() + "] TreeFeature: " + x + ", " + z + ", " + y);
            }

            var trunkHeight = this.random.nextInt(this.minTrunkHeight, this.maxTrunkHeight);

            for (int blkY = 1; blkY < trunkHeight; blkY++) {
                setter.set(x, blkY, z, this.block.getDefaultState());
            }

            if (WorldGenDebugContext.isActive()) {
                System.out.println("[End " + Thread.currentThread().threadId() + "] TreeFeature: " + x + ", " + z + ", " + y + " - Success");
            }
            return true;
        }

        return false;
    }

    @Override
    public void create(@NotNull ServerWorld world) {

    }
}
