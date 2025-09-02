package dev.ultreon.quantum.entity.player;

import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Temperature {
    private final double temperature;

    public Temperature(double temperature) {
        this.temperature = temperature;
    }

    /**
     * Returns true if the temperature is cold.
     * Cold means below 10 °C.
     *
     * @return true if the temperature is cold.
     */
    public boolean isCold() {
        return convertTo(TemperatureUnit.CELSIUS) < 10.0;
    }

    /**
     * Returns true if the temperature is warm.
     * Warm means above 25 °C.
     *
     * @return true if the temperature is warm.
     */
    public boolean isWarm() {
        return convertTo(TemperatureUnit.CELSIUS) > 25.0;
    }

    /**
     * Returns the temperature in Celsius.
     *
     * @return the temperature in Celsius.
     * @deprecated use {@link #convertTo(TemperatureUnit)} instead.
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    public double celsius() {
        return convertTo(TemperatureUnit.CELSIUS);
    }

    public double convertTo(TemperatureUnit format) {
        return format.convertFromInternal(temperature);
    }

    /**
     * Represents a temperature range.
     * For example, if the temperature is 30 °C (or 102 °F), the range is HOT.
     * The minimum temperature is inclusive, while the maximum temperature is exclusive.
     *
     * @author XyperCode
     * @since 0.1.0
     */
    public enum Range {
        TOO_HOT(45.0, Double.POSITIVE_INFINITY),
        HOT(30.0, 45.0),
        WARM(22.0, 28.0),

        /**
         * Around room temperature.
         */
        LUKE_WARM(19.0, 22.0),
        NORMAL(12.0, 19.0),
        CHILLY(6.0, 12.0),
        COLD(0.0, 11.0),
        FREEZING(Double.NEGATIVE_INFINITY, 0.0);

        private final double min;

        private final double max;

        Range(double min, double max) {
            this.min = min;
            this.max = max;
        }

        /**
         * @return the minimum (inclusive) temperature in the range.
         */
        public double getMin() {
            return min;
        }

        /**
         * @return the maximum (exclusive) temperature in the range.
         */
        public double getMax() {
            return max;
        }

    }

    /**
     * See {@link Range} for more information.
     *
     * @since 0.1.0
     */
    public Range getType() {
        var celsius = convertTo(TemperatureUnit.CELSIUS);
        if (celsius >= 40.0) return Range.TOO_HOT; // This is >= 104 "F
        if (celsius >= 30.0) return Range.HOT; // This is >= 86 "F
        else if (celsius >= 22.0) return Range.WARM; // This is >= 71 °F
        else if (celsius >= 19.0) return Range.LUKE_WARM; // This is >= 66 °F
        else if (celsius >= 12.0) return Range.NORMAL; // This is >= 53 °F
        else if (celsius >= 6.0) return Range.CHILLY; // This is >= 42 °F
        else if (celsius >= 0.0) return Range.COLD; // This is >= 32 °F
        else if (celsius < 0.0) return Range.FREEZING; // This is < 32 °F
        return Range.NORMAL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (Temperature) o;
        return Double.compare(that.temperature, temperature) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature);
    }

    @Override
    public String toString() {
        return toString(TemperatureUnit.CELSIUS);
    }

    public String toString(TemperatureUnit unit) {
        return unit.convertToInternal(temperature) + " " + unit.getCode();
    }

    public int compareTo(@NotNull Temperature temperature) {
        return Double.compare(temperature.temperature, this.temperature);
    }

    public static double getTemperature(ServerWorld world, int x, int y, int z) {
        return getBiomeTemp(world, x, z);
    }

    private static double getWaterBasedTemp(@NotNull Player player, World world, BlockVec pos, double temperature) {
        // TODO: Uncomment this when weather is added
//        var isInRain = world.isRainingAt(pos) || world.isRainingAt(new BlockPos(pos.getX(), (int) player.getBoundingBox().maxY, pos.getZ()));

        // Check for water temperature.
        if (!player.isInWater()) {
            temperature -= 0.32;
//        } else if (isInRain) {
//            temperature -= 0.1;
        }
        return temperature;
    }

    private static double getTimeBasedTemp(ServerWorld world, int x, int z, double temperature) {
        // Get the time, used for temperature change over day.
        var time = (int) (world.getTime() % 24000L);

        // Calculate temperature based on day.
        if (time > 15000) {
            // Night
            var timeTempModifier = getNightTempSub(world, x, z, temperature);

            temperature -= timeTempModifier;
        } else if (time < 4000) {
            // Morning
            var upRise = 1 - (double) time / 4000;
            var timeTempModifier = getNightTempSub(world, x, z, temperature);

            temperature -= timeTempModifier * upRise;
        } else if (time <= 11000) {
            // Noon
            temperature += 0;
        } else {
            // Afternoon
            var downFall = (time - 11000.0) / 4000.0;
            var timeTempModifier = getNightTempSub(world, x, z, temperature);

            temperature -= timeTempModifier * downFall;
        }
        return temperature;
    }

    private static double getNightTempSub(ServerWorld world, int x, int z, double temperature) {
        var funcA = temperature + 1;
        var funcB = Mth.clamp(funcA, 0, 3);
        var funcC = 2.5 - funcB;
        var clamp = Mth.clamp(funcC, 0, 3);
        double baseTemperature = getBiomeTemp(world, x, z);
        return clamp / 1.5 * ((Math.pow(Math.max(baseTemperature - 1.0, 0.1), 1.25)) / 2.0);
    }

    private static double getBiomeTemp(ServerWorld world, int x, int z) {
        return world.getGenerator().getTemperature(x, z);
    }

    public double temperature() {
        return temperature;
    }

}
