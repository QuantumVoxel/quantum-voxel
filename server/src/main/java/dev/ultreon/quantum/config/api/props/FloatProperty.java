package dev.ultreon.quantum.config.api.props;

import com.badlogic.gdx.utils.JsonValue;
import org.jetbrains.annotations.NotNull;

public class FloatProperty implements ConfigProperty<Float> {
    private final String name;
    private float value;
    private final float fallback;
    private final float min;
    private final float max;

    public FloatProperty(String name, float fallback, float min, float max) {
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
    public @NotNull Float getValue() {
        return value;
    }

    @Override
    public void setValue(Float value) {
        if (value < min) value = min;
        if (value > max) value = max;
        this.value = value;
    }

    @Override
    public boolean isValid(Float value) {
        if (value == null) return false;
        return value >= min && value <= max;
    }

    public Float getFallback() {
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
        this.value = value.asFloat();
    }
}
