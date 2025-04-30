package dev.ultreon.quantum.config.api.props;

import com.badlogic.gdx.utils.JsonValue;

public class IntProperty implements ConfigProperty<Integer> {
    private final String name;
    private int value;
    private final int fallback;
    private final int min;
    private final int max;

    public IntProperty(String name, int fallback, int min, int max) {
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
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        if (value < min) value = min;
        if (value > max) value = max;
        this.value = value;
    }

    @Override
    public boolean isValid(Integer value) {
        if (value == null) return false;
        return value >= min && value <= max;
    }

    public Integer getFallback() {
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
        this.value = value.asInt();
    }
}
