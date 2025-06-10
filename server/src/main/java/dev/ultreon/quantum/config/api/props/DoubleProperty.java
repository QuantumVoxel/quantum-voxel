package dev.ultreon.quantum.config.api.props;

import com.badlogic.gdx.utils.JsonValue;
import org.jetbrains.annotations.NotNull;

public class DoubleProperty implements ConfigProperty<Double> {
    private final String name;
    private double value;
    private final double fallback;
    private final double min;
    private final double max;

    public DoubleProperty(String name, double fallback, double min, double max) {
        this.name = name;
        this.value = fallback;
        this.fallback = fallback;
        this.min = min;
        this.max = max;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @NotNull Double getValue() {
        return value;
    }

    @Override
    public void setValue(Double value) {
        if (value < min) value = min;
        if (value > max) value = max;
        this.value = value;
    }

    @Override
    public boolean isValid(Double value) {
        if (value == null) return false;
        return value >= min && value <= max;
    }

    public Double getFallback() {
        return fallback;
    }

    @Override
    public JsonValue getJson() {
        return new JsonValue(value);
    }

    @Override
    public void reset() {
        value = fallback;
    }

    @Override
    public void setJson(JsonValue value) {
        this.value = value.asDouble();
    }
}
