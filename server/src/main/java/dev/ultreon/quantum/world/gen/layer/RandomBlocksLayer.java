package dev.ultreon.quantum.world.gen.layer;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.world.BlockSetter;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import dev.ultreon.quantum.world.gen.noise.NoiseInstance;
import dev.ultreon.quantum.world.rng.RNG;
import org.jetbrains.annotations.NotNull;

public abstract class RandomBlocksLayer extends TerrainLayer {
    private final int from;
    private final int to;
    private final Block[] blocks;

    protected RandomBlocksLayer(int from, int to, Block[] blocks) {
        this.from = from;
        this.to = to;
        this.blocks = blocks;
    }

    public static RandomBlocksLayer surface(int thickness, int from, int to, Block... blocks) {
        return new RandomBlocksLayer(from, to, blocks) {
            public boolean shouldGenerate(int x, int y, int z, int height) {
                return y >= height - thickness && y < height;
            }
        };
    }

    public static RandomBlocksLayer underground(@NotNull NoiseConfig noiseConfig, double threshold, int from, int to, Block... blocks) {
        return new RandomBlocksLayer(from, to, blocks) {
            private NoiseInstance noise;

            @Override
            public void create(@NotNull ServerWorld world) {
                super.create(world);

                this.noise = noiseConfig.create(world.getSeed() + noiseConfig.seed());
            }

            @Override
            public void dispose() {
                super.dispose();

                this.noise.dispose();
                this.noise = null;
            }

            public boolean shouldGenerate(int x, int y, int z, int height) {
                return y < height && noise.eval(x, y, z) < threshold;
            }
        };
    }

    @Override
    public boolean handle(@NotNull World world, BlockSetter chunk, @NotNull RNG rng, int x, int y, int z, int height) {
        if (from <= y && y <= to && y < height && shouldGenerate(x, y, z, height)) {
            Block block = blocks[rng.nextInt(blocks.length)];
            chunk.set(x, y, z, block.getDefaultState());
            return true;
        }

        return false;
    }

    protected abstract boolean shouldGenerate(int x, int y, int z, int height);
}
