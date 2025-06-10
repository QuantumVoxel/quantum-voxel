package dev.ultreon.quantum.config.api.props;

import com.badlogic.gdx.utils.JsonValue;
import org.jetbrains.annotations.NotNull;

public class ShortProperty implements ConfigProperty<Short> {
    private final String name;
    private short value;
    private final short fallback;
    private final short min;
    private final short max;

    public ShortProperty(String name, short fallback, short min, short max) {
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
    public @NotNull Short getValue() {
        return value;
    }

    @Override
    public void setValue(Short value) {
        if (value < min) value = min;
        if (value > max) value = max;
        this.value = value;
    }

    @Override
    public boolean isValid(Short value) {
        if (value == null) return false;
        return value >= min && value <= max;
    }

    public Short getFallback() {
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
        this.value = value.asShort();
    }
}
