package dev.ultreon.quantum.world.gen.noise;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;

import java.util.Objects;

public final class JNoiseType implements NoiseType {
    private final NoiseSource source;

    public JNoiseType(NoiseSource source) {
        this.source = source;
    }

    @Override
    public void dispose() {

    }

    @Override
    public double eval(double x, double y) {
        return source.evaluateNoise(x, y) + 1 / 2.0;
    }

    @Override
    public double eval(double x, double y, double z) {
        return source.evaluateNoise(x, y, z) + 1 / 2.0;
    }

    public NoiseSource source() {
        return source;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (JNoiseType) obj;
        return Objects.equals(this.source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }

    @Override
    public String toString() {
        return "JNoiseType[" +
               "source=" + source + ']';
    }

}
