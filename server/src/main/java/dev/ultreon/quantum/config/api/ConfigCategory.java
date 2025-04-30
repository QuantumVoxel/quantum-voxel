package dev.ultreon.quantum.config.api;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Predicate;
import dev.ultreon.quantum.config.api.props.*;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.HashMap;
import java.util.Map;

public class ConfigCategory implements ConfigValue {
    private final Map<String, ConfigValue> properties = new HashMap<>();

    public BooleanProperty create(String name, boolean fallback) {
        var property = new BooleanProperty(name, fallback);
        properties.put(name, property);
        return property;
    }

    public StringProperty create(String name, String fallback, Predicate<String> validation) {
        var property = new StringProperty(name, fallback, validation);
        properties.put(name, property);
        return property;
    }

    public NamespaceIDProperty create(String name, NamespaceID fallback, Predicate<NamespaceID> validation) {
        var property = new NamespaceIDProperty(name, fallback, validation);
        properties.put(name, property);
        return property;
    }

    public ByteProperty create(String name, byte fallback, byte min, byte max) {
        var property = new ByteProperty(name, fallback, min, max);
        properties.put(name, property);
        return property;
    }

    public ShortProperty create(String name, short fallback, short min, short max) {
        var property = new ShortProperty(name, fallback, min, max);
        properties.put(name, property);
        return property;
    }

    public IntProperty create(String name, int fallback, int min, int max) {
        var property = new IntProperty(name, fallback, min, max);
        properties.put(name, property);
        return property;
    }

    public LongProperty create(String name, long fallback, long min, long max) {
        var property = new LongProperty(name, fallback, min, max);
        properties.put(name, property);
        return property;
    }

    public FloatProperty create(String name, float fallback, float min, float max) {
        var property = new FloatProperty(name, fallback, min, max);
        properties.put(name, property);
        return property;
    }

    public DoubleProperty create(String name, double fallback, double min, double max) {
        var property = new DoubleProperty(name, fallback, min, max);
        properties.put(name, property);
        return property;
    }

    public ConfigValue get(String name) {
        return properties.get(name);
    }

    @Override
    public JsonValue getJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        for (String name : properties.keySet()) {
            json.addChild(name, properties.get(name).getJson());
        }

        return json;
    }

    @Override
    public void setJson(JsonValue value) {
        for (JsonValue json : value) {
            properties.get(json.name).setJson(json);
        }
    }

    @Override
    public void reset() {
        for (ConfigValue value : properties.values()) {
            value.reset();
        }
    }

    public ConfigCategory createCategory(String name) {
        ConfigCategory value = new ConfigCategory();
        this.properties.put(name, value);
        return value;
    }
}
