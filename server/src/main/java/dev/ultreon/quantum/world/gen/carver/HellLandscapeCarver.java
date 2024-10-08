
package dev.ultreon.quantum.world.gen.carver;

import de.articdive.jnoise.core.api.functions.Interpolation;
import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction;
import de.articdive.jnoise.pipeline.JNoise;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.vec.BlockVec;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

public class HellLandscapeCarver implements Carver {
    private final NoiseSource source;

    public HellLandscapeCarver(long seed) {
        this.source = JNoise.newBuilder()
                .perlin(seed, Interpolation.COSINE, FadeFunction.CUBIC_POLY)
                .scale(1 / 32.0f)
                .build();
    }

    @Override
    public int carve(BuilderChunk chunk, int x, int z) {
        BlockVec offset = chunk.getOffset();
        for (int y = offset.y; y < offset.y + CHUNK_SIZE; y++) {
            double noise = this.source.evaluateNoise(x, y, z);
            if (noise < 0.1) {
                chunk.set(x, y, z, Blocks.COBBLESTONE.createMeta());
            }
        }

        return -1;
    }

    @Override
    public int getSurfaceHeightNoise(float x, float z) {
        return 0;
    }

    @Override
    public boolean isAir(int x, int y, int z) {
        double noise = this.source.evaluateNoise(x, y, z);
        return noise >= 0.1;
    }
}
