package dev.ultreon.quantum.world.gen;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex4DVariant;
import de.articdive.jnoise.pipeline.JNoise;

public class HillinessNoise implements NoiseSource {
    private final JNoise noise;

    public HillinessNoise(long seed) {
        this.noise = JNoise.newBuilder()
                .fastSimplex(seed, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                .scale(1 / 32.0f)
                .addModifier((value) -> value * 2)
                .build();
    }

    @Override
    public double evaluateNoise(double x) {
        return this.noise.evaluateNoise(x / 32) + 2.0f;
    }

    @Override
    public double evaluateNoise(double x, double y) {
        return this.noise.evaluateNoise(x / 32, y / 32) + 2.0f;
    }

    @Override
    public double evaluateNoise(double x, double y, double z) {
        return this.noise.evaluateNoise(x, y, z) + 2.0f;
    }

    @Override
    public double evaluateNoise(double x, double y, double z, double w) {
        return this.noise.evaluateNoise(x, y, z, w) + 2.0f;
    }
}
