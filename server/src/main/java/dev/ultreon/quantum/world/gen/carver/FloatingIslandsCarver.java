package dev.ultreon.quantum.world.gen.carver;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex4DVariant;
import de.articdive.jnoise.pipeline.JNoise;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.vec.BlockVec;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

public class FloatingIslandsCarver implements Carver {
    private final NoiseSource source;

    public FloatingIslandsCarver(long seed) {
        this.source = JNoise.newBuilder()
                .fastSimplex(seed, Simplex2DVariant.CLASSIC, Simplex3DVariant.CLASSIC, Simplex4DVariant.CLASSIC)
                .scale(1 / 32.0f)
                .build();
    }

    @Override
    public int carve(BuilderChunk chunk, int x, int z) {
        BlockVec offset = chunk.getOffset();
        for (int y = 0; y < CHUNK_SIZE; y++) {
            double noise = this.source.evaluateNoise(x, offset.y + y, z);
            if (noise > 0.7) {
                chunk.set(x, y, z, Blocks.COBBLESTONE.getDefaultState());
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
        return noise <= 0.7;
    }
}
