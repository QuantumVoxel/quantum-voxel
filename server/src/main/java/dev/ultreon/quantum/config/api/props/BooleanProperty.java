package dev.ultreon.quantum.config.api.props;

import com.badlogic.gdx.utils.JsonValue;

public class BooleanProperty implements ConfigProperty<Boolean> {
    private boolean value;
    private final String name;
    private final boolean fallback;

    public BooleanProperty(String name, boolean fallback) {
        this.value = fallback;
        this.name = name;
        this.fallback = fallback;
    }

    public String getName() {
        return name;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public boolean isValid(Boolean value) {
        return value != null;
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
        this.value = value.asBoolean();
    }

    public Boolean getFallback() {
        return fallback;
    }
}
