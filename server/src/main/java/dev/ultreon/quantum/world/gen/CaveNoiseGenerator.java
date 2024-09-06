package dev.ultreon.quantum.world.gen;

import de.articdive.jnoise.core.api.functions.Interpolation;
import de.articdive.jnoise.core.api.noisegen.NoiseGenerator;
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;

import static java.lang.Math.abs;

/**
 * This is a noise generator that generates cave-like terrain.
 * Values are either zero or one. Where 1 is air and 0 is solid.
 *
 * @author XyperCode
 */
public class CaveNoiseGenerator implements NoiseGenerator {
    public static final double SCALE = 24;
    public static final double THRESHOLD = 0.4;
    private final JNoise baseNoise;
    private final JNoise baseNoise2;
    private final JNoise baseNoise3;
    private final WorleyUtil worley;

    public CaveNoiseGenerator(long seed) {
        this.baseNoise = newNoiseBuilder(seed).build();
        this.baseNoise2 = newNoiseBuilder(seed + 10).build();
        this.baseNoise3 = newNoiseBuilder(seed + 20).build();
        worley = new WorleyUtil((int) (seed + 100));
    }

    private static JNoise.JNoiseBuilder<?> newNoiseBuilder(long seed) {
        return JNoise.newBuilder()
                .perlin(seed + 50, Interpolation.LINEAR, FadeFunction.QUINTIC_POLY)
                .octavate(2, 0.1, 3, FractalFunction.TURBULENCE, true)
                .scale(1 / 24.0)
                .abs()
                ;
    }

    @Override
    public double evaluateNoise(double x) {
        return baseNoise.evaluateNoise(x);
    }

    @Override
    public double evaluateNoise(double x, double y) {
        return abs(worley.SingleCellular3Edge(x, y, 0)) < THRESHOLD - 0.3 ? 1 : 0;
    }

    @Override
    public double evaluateNoise(double x, double y, double z) {
        y *= 1;
        x *= 1;
        int distort = 16;

        double xDisp;
        double yDisp;
        double zDisp;

        xDisp = baseNoise.evaluateNoise(x, z) * distort;
        yDisp = baseNoise2.evaluateNoise(x, z + 67.0f) * distort;
        zDisp = baseNoise3.evaluateNoise(x, z + 149.0f) * distort;

        // doubling the y frequency to get some more caves
        double yCompression = 2.0;
        double xzCompression = 1.0;
        double noise = worley.SingleCellular3Edge(x * xzCompression + xDisp, y * yCompression + yDisp, z * xzCompression + zDisp);

        return abs(noise) < 0.18 ? 1 : 0;
    }

    @Override
    public double evaluateNoise(double x, double y, double z, double w) {
        return baseNoise.evaluateNoise(x, y, z, w) > THRESHOLD ? 1 : 0;
    }
}
