package dev.ultreon.quantum.world.gen.biome;

import java.util.Objects;

/**
 * A record that encapsulates various environmental threshold values required to define a biome.
 *
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
public record BiomeData(float temperatureStartThreshold, float temperatureEndThreshold, float humidityStartThreshold,
                        float humidityEndThreshold, float heightStartThreshold, float heightEndThreshold,
                        float hillinessStartThreshold, float hillinessEndThreshold, boolean isOcean, BiomeGenerator biomeGen) {

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
}
