package dev.ultreon.quantum.world.gen.biome;

import java.util.Objects;

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
        return -1.0f;
    }

    public double variationEndThreshold() {
        return 1.0f;
    }
}
