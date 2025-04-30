package dev.ultreon.quantum.config.api.props;

import com.badlogic.gdx.utils.JsonValue;

public class ByteProperty implements ConfigProperty<Byte> {
    private final String name;
    private byte value;
    private final byte fallback;
    private final byte min;
    private final byte max;

    public ByteProperty(String name, byte fallback, byte min, byte max) {
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
    public Byte getValue() {
        return value;
    }

    @Override
    public void setValue(Byte value) {
        if (value < min) value = min;
        if (value > max) value = max;
        this.value = value;
    }

    @Override
    public boolean isValid(Byte value) {
        if (value == null) return false;
        return value >= min && value <= max;
    }

    public Byte getFallback() {
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
        this.value = value.asByte();
    }
}
