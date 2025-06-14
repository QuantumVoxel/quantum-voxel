package dev.ultreon.quantum.world.gen.feature;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.world.Fork;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.gen.TerrainFeature;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import dev.ultreon.quantum.world.gen.noise.NoiseInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchFeature extends TerrainFeature {
    private final NoiseConfig settingsBase;
    private final Block patchBlock;
    private final float threshold;
    @Nullable
    private NoiseInstance baseNoise;
    private final int depth;

    /**
     * Creates a new patch feature with the given settings
     *
     * @param settingsBase the noise config to use
     * @param patchBlock   the block to use for the patch
     * @param threshold    the threshold to use for the patch
     * @deprecated Use {@link #PatchFeature(NoiseConfig, Block, float, int)} instead
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    public PatchFeature(NoiseConfig settingsBase, Block patchBlock, float threshold) {
        this(settingsBase, patchBlock, threshold, 4);
    }

    /**
     * Creates a new patch feature with the given settings
     *
     * @param settingsBase the noise config to use
     * @param patchBlock   the block to use for the patch
     * @param threshold    the threshold to use for the patch
     * @param depth        the depth for the patch generation.
     */
    public PatchFeature(NoiseConfig settingsBase, Block patchBlock, float threshold, int depth) {
        this.settingsBase = settingsBase;
        this.patchBlock = patchBlock;
        this.threshold = threshold;
        this.depth = depth;
    }

    @Override
    public void create(@NotNull ServerWorld world) {
        super.create(world);

        this.baseNoise = this.settingsBase.create(world.getSeed());
    }

    @Override
    public boolean handle(@NotNull Fork setter, long seed, int x, int y, int z) {
        if (this.baseNoise == null) return false;

        boolean changed = false;
        for (int blkY = -this.depth; blkY < 0; blkY++) {
            float value = (float) this.baseNoise.eval(x, blkY + y, z);
            changed |= value < this.threshold && setter.set(0, blkY, z, this.patchBlock.getDefaultState());
        }

        return changed;
    }

    @Override
    public void dispose() {
        if (this.baseNoise != null) this.baseNoise.dispose();
    }
}
