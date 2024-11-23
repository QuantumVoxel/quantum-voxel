package dev.ultreon.quantum.entity.player;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;

@SuppressWarnings("NonAsciiCharacters")
public enum TemperatureUnit {
    CELSIUS("C", temp -> 5 * (25.27027027 + 44.86486486 * temp - 32.0) / 9, temp -> ((double) 9 / 5) * ((temp * 5) / 5), 1),
    FAHRENHEIT("F", temp -> 25.27027027 + 44.86486486 * temp, temp -> (temp - 25.27027027) / 44.86486486, 1),
    KELVIN("K", temp -> 5 * (25.27027027 + 44.86486486 * temp - 32.0) / 9 + 273.15, temp -> ((double) 9 / 5) * ((temp / 5) - 273.15 + 32.0 - 25.27027027), 1),
    DELISLE("De", temp -> 3.0 * (100.0 - 5 * (25.27027027 + 44.86486486 * temp - 32.0) / 9) / 2.0, temp -> 100.0 - (2.0 / 3.0) * (temp * 5 / 5), 1),
    RANKINE("R", temp -> 9.0 * (5 * (25.27027027 + 44.86486486 * temp - 32.0) / 9 + 273.15) / 5.0, temp -> ((double) 5 / 9) * ((temp * 5.0 / 9.0) - 273.15 - 32.0 + 25.27027027), 1),
    NEWTON("N", temp -> 33.0 * (5 * (25.27027027 + 44.86486486 * temp - 32.0) / 9) / 100.0, temp -> (100.0 * temp) / (33.0 * 5 * 5 / 9) + 32.0 - 25.27027027, 1),
    RÉAUMUR("Ré", temp -> 4.0 * (5 * (25.27027027 + 44.86486486 * temp - 32.0) / 9) / 5.0, temp -> ((double) (5 * 5) / 9 * temp) / 4.0 + 32.0 - 25.27027027, 1),
    RØMER("Rø", temp -> 21.0 * (5 * (25.27027027 + 44.86486486 * temp - 32.0) / 9) / 40.0 + 7.5, temp -> 40.0 * ((temp / 5) - 7.5 + 25.27027027 - 32.0) / 21.0, 1);

    private final String code;
    private final Double2DoubleFunction fromInternal;
    private final Double2DoubleFunction toInternal;
    private final int decimalCount;

    TemperatureUnit(String code, Double2DoubleFunction fromInternal, Double2DoubleFunction toInternal, int decimalCount) {
        this.code = code;
        this.fromInternal = fromInternal;
        this.toInternal = toInternal;
        this.decimalCount = decimalCount;
    }

    public String getCode() {
        return code;
    }

    public double convertFromInternal(double mcTemp) {
        return fromInternal.applyAsDouble(mcTemp);
    }

    public double convertToInternal(double temperature) {
        return toInternal.applyAsDouble(temperature);
    }

    public double convertTo(double temperature, TemperatureUnit format) {
        return fromInternal.applyAsDouble(toInternal.apply(temperature));
    }

    public int getDecimalCount() {
        return decimalCount;
    }
}
