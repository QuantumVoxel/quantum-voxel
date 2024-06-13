package dev.ultreon.quantum.world.gen.biome;

import java.util.Objects;

public final class BiomeData {
    private final float temperatureStartThreshold;
    private final float temperatureEndThreshold;
    private final float humidityStartThreshold;
    private final float humidityEndThreshold;
    private final boolean isOcean;
    private final BiomeGenerator biomeGen;

    public BiomeData(float temperatureStartThreshold, float temperatureEndThreshold, float humidityStartThreshold, float humidityEndThreshold, boolean isOcean, BiomeGenerator biomeGen) {
        this.temperatureStartThreshold = temperatureStartThreshold;
        this.temperatureEndThreshold = temperatureEndThreshold;
        this.humidityStartThreshold = humidityStartThreshold;
        this.humidityEndThreshold = humidityEndThreshold;
        this.isOcean = isOcean;
        this.biomeGen = biomeGen;
    }

    @Override
    public String toString() {
        return "BiomeData{" +
               "temperatureStartThreshold=" + this.temperatureStartThreshold +
               ", temperatureEndThreshold=" + this.temperatureEndThreshold +
               ", biomeGen=" + this.biomeGen +
               '}';
    }

    public float temperatureStartThreshold() {
        return temperatureStartThreshold;
    }

    public float temperatureEndThreshold() {
        return temperatureEndThreshold;
    }

    public float humidityStartThreshold() {
        return humidityStartThreshold;
    }

    public float humidityEndThreshold() {
        return humidityEndThreshold;
    }

    public boolean isOcean() {
        return isOcean;
    }

    public BiomeGenerator biomeGen() {
        return biomeGen;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BiomeData) obj;
        return Float.floatToIntBits(this.temperatureStartThreshold) == Float.floatToIntBits(that.temperatureStartThreshold) &&
               Float.floatToIntBits(this.temperatureEndThreshold) == Float.floatToIntBits(that.temperatureEndThreshold) &&
               this.isOcean == that.isOcean &&
               Objects.equals(this.biomeGen, that.biomeGen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperatureStartThreshold, temperatureEndThreshold, isOcean, biomeGen);
    }

}
