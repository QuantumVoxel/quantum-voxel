package dev.ultreon.quantum.config.api.props;

import com.badlogic.gdx.utils.JsonValue;

public class LongProperty implements ConfigProperty<Long> {
    private final String name;
    private long value;
    private final long fallback;
    private final long min;
    private final long max;

    public LongProperty(String name, long fallback, long min, long max) {
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
    public Long getValue() {
        return value;
    }

    @Override
    public void setValue(Long value) {
        if (value < min) value = min;
        if (value > max) value = max;
        this.value = value;
    }

    @Override
    public boolean isValid(Long value) {
        if (value == null) return false;
        return value >= min && value <= max;
    }

    public Long getFallback() {
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
        this.value = value.asLong();
    }
}
