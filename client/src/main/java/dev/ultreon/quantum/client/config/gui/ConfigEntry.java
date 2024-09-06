package dev.ultreon.quantum.client.config.gui;

import com.google.common.base.Preconditions;
import dev.ultreon.quantum.client.config.entries.*;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.config.crafty.Ranged;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class ConfigEntry<T> {
    private final String key;
    private final T defaultValue;
    protected T value;
    @Nullable
    private String comment;
    protected final CraftyConfig config;

    protected ConfigEntry(String key, T value, CraftyConfig config) {
        this.key = key;
        this.value = value;
        this.defaultValue = value;
        this.config = config;
    }

    public static @Nullable ConfigEntry<?> of(String key, Object value, CraftyConfig config) {
        final Ranged range = config.getRange(key);
        switch (value) {
            case String s -> {
                return new StringEntry(key, s, config);
            }

            case Boolean b -> {
                return new BooleanEntry(key, b, config);
            }

            case Long l -> {
                return new LongEntry(key, l, range == null ? -1000L : (long) range.min(), range == null ? 1000L : (long) range.max(), config);
            }

            case Double d -> {
                return new DoubleEntry(key, d, range == null ? -1000 : range.min(), range == null ? 1000 : range.max(), config);
            }

            case Integer i -> {
                return new IntEntry(key, i, range == null ? -1000 : (int) range.min(), range == null ? 1000 : (int) range.max(), config);
            }

            case Float f -> {
                return new FloatEntry(key, f, range == null ? -1000f : (float) range.min(), range == null ? 1000f : (float) range.max(), config);
            }

            case UUID u -> {
                return new UUIDEntry(key, u, config);
            }

            case Enum e -> {
                return new EnumEntry(key, e, config);
            }

            default -> {
                return null;
            }
        }
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        Preconditions.checkNotNull(value, "Cannot set config value to null");
        this.value = value;

        this.config.set(this.key, value);
    }

    public ConfigEntry<T> comment(String comment) {
        Preconditions.checkNotNull(comment, "Cannot add null comment");
        this.comment = comment;
        return this;
    }

    protected abstract T read(String text);

    public void readAndSet(String text) {
        try {
            this.value = this.read(text);
        } catch (Exception ignored) {

        }
    }

    @Nullable
    public String getComment() {
        return this.config.getComment(this.key);
    }

    public String getKey() {
        return this.key;
    }

    public String write() {
        return this.value.toString();
    }

    public String getDescription() {
        return Language.translate("config." + this.config.getFileName().replace(".json5", "") + "." + this.getKey());
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public abstract Widget createWidget();
}
