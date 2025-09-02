package dev.ultreon.quantum.world.gen.biome;

import java.util.Objects;

/**
 * A record that encapsulates various environmental threshold values required to define a biome.
 *
 */
public final class BiomeData {
    private final float temperatureStartThreshold;
    private final float temperatureEndThreshold;
    private final float humidityStartThreshold;
    private final float humidityEndThreshold;
    private final float heightStartThreshold;
    private final float heightEndThreshold;
    private final float hillinessStartThreshold;
    private final float hillinessEndThreshold;
    private final boolean isOcean;
    private final BiomeGenerator biomeGen;

    /**
     * @param temperatureStartThreshold The starting threshold for biome temperature.
     * @param temperatureEndThreshold The ending threshold for biome temperature.
     * @param humidityStartThreshold The starting threshold for biome humidity.
     * @param humidityEndThreshold The ending threshold for biome humidity.
     * @param heightStartThreshold The starting threshold for biome height.
     * @param heightEndThreshold The ending threshold for biome height.
     * @param hillinessStartThreshold The starting threshold for biome hilliness.
     * @param hillinessEndThreshold The ending threshold for biome hilliness.
     * @param isOcean Flag indicating if the biome is an ocean.
     * @param biomeGen The biome generator responsible for generating the biome.
     */
    public BiomeData(float temperatureStartThreshold, float temperatureEndThreshold, float humidityStartThreshold,
                     float humidityEndThreshold, float heightStartThreshold, float heightEndThreshold,
                     float hillinessStartThreshold, float hillinessEndThreshold, boolean isOcean, BiomeGenerator biomeGen) {
        this.temperatureStartThreshold = temperatureStartThreshold;
        this.temperatureEndThreshold = temperatureEndThreshold;
        this.humidityStartThreshold = humidityStartThreshold;
        this.humidityEndThreshold = humidityEndThreshold;
        this.heightStartThreshold = heightStartThreshold;
        this.heightEndThreshold = heightEndThreshold;
        this.hillinessStartThreshold = hillinessStartThreshold;
        this.hillinessEndThreshold = hillinessEndThreshold;
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

    public double variationStartThreshold() {
        return -2.0f;
    }

    public double variationEndThreshold() {
        return 2.0f;
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

    public float heightStartThreshold() {
        return heightStartThreshold;
    }

    public float heightEndThreshold() {
        return heightEndThreshold;
    }

    public float hillinessStartThreshold() {
        return hillinessStartThreshold;
    }

    public float hillinessEndThreshold() {
        return hillinessEndThreshold;
    }

    public boolean isOcean() {
        return isOcean;
    }

    public BiomeGenerator biomeGen() {
        return biomeGen;
    }

}
