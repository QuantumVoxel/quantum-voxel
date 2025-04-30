package dev.ultreon.quantum.config.api;

import com.badlogic.gdx.utils.JsonValue;

public interface ConfigValue {
    JsonValue getJson();

    void reset();

    void setJson(JsonValue value);
}
