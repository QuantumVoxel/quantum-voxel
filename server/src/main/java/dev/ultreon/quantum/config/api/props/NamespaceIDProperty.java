package dev.ultreon.quantum.config.api.props;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Predicate;
import dev.ultreon.quantum.util.NamespaceID;

public class NamespaceIDProperty implements ConfigProperty<NamespaceID> {
    private NamespaceID value;
    private final String name;
    private final NamespaceID fallback;
    private final Predicate<NamespaceID> verification;

    public NamespaceIDProperty(String name, NamespaceID fallback, Predicate<NamespaceID> verification) {
        this.value = fallback;
        this.name = name;
        this.fallback = fallback;
        this.verification = verification;
    }

    public String getName() {
        return name;
    }

    @Override
    public NamespaceID getValue() {
        return value;
    }

    @Override
    public void setValue(NamespaceID value) {
        if (!verification.evaluate(value)) return;
        this.value = value;
    }

    @Override
    public boolean isValid(NamespaceID value) {
        return value != null;
    }

    @Override
    public JsonValue getJson() {
        return new JsonValue(value.toString());
    }

    @Override
    public void reset() {
        value = fallback;
    }

    @Override
    public void setJson(JsonValue value) {
        NamespaceID parsed = NamespaceID.tryParse(value.asString());
        if (parsed == null) return;
        this.value = parsed;
    }

    @Override
    public NamespaceID getFallback() {
        return fallback;
    }
}
