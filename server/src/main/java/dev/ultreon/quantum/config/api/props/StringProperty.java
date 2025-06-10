package dev.ultreon.quantum.config.api.props;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Predicate;
import org.jetbrains.annotations.NotNull;

public class StringProperty implements ConfigProperty<String> {
    private String value;
    private final String name;
    private final String fallback;
    private final Predicate<String> verification;

    public StringProperty(String name, String fallback, Predicate<String> verification) {
        this.value = fallback;
        this.name = name;
        this.fallback = fallback;
        this.verification = verification;
    }

    public String getName() {
        return name;
    }

    @Override
    public @NotNull String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        if (!verification.evaluate(value)) return;
        this.value = value;
    }

    @Override
    public boolean isValid(String value) {
        return value != null && verification.evaluate(value);
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
        this.value = value.asString();
    }

    @Override
    public String getFallback() {
        return fallback;
    }
}
