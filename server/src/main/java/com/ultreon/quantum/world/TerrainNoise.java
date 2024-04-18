package com.ultreon.quantum.world;

import de.articdive.jnoise.core.api.functions.Combiner;
import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex4DVariant;
import de.articdive.jnoise.pipeline.JNoise;

public class TerrainNoise implements NoiseSource {
    private final NoiseSource noise;

    public TerrainNoise(long seed) {
        noise = JNoise.newBuilder()
                .fastSimplex(seed, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                .combine(
                        // Smaller island generation
                        JNoise.newBuilder().fastSimplex(seed + 1, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 4096f)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed + 1, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 256f)
                                .addModifier(result -> result * 24)
                                .build(),
                        Combiner.ADD
                ).combine(
                        // Smaller island generation
                        JNoise.newBuilder().fastSimplex(seed + 1, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 2048f)
                                .addModifier(result -> result * 8)
                                .build(),
                        Combiner.ADD
                ).combine(
                        // Smaller island generation
                        JNoise.newBuilder().fastSimplex(seed + 1, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 1024f)
                                .addModifier(result -> result * 8)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed + 2, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 128f)
                                .addModifier(result -> result * 8)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed + 2, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 64f)
                                .addModifier(result -> result * 20)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed + 2, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 16f)
                                .addModifier(result -> result * 14)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed + 3, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 8f)
                                .addModifier(result -> result * 16)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed + 4, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 8f)
                                .addModifier(result -> result * 16)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed + 4, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 4f)
                                .addModifier(result -> result * 8)
                                .build(),
                        Combiner.ADD
                )
                .addModifier(result -> result < 64 ? (64 - result) / 3 + 1 / 3.0 + 64 : result)
                .addModifier(result -> result)
                .scale(1 / 16f).build();
    }

    @Override
    public double evaluateNoise(double x) {
        return noise.evaluateNoise(x);
    }

    @Override
    public double evaluateNoise(double x, double y) {
        return noise.evaluateNoise(x, y);
    }

    @Override
    public double evaluateNoise(double x, double y, double z) {
        return noise.evaluateNoise(x, y, z);
    }

    @Override
    public double evaluateNoise(double x, double y, double z, double w) {
        return noise.evaluateNoise(x, y, z, w);
    }
}
