package dev.ultreon.quantum.client.input.controller.gui;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.client.input.controller.Config;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class ConfigEntry<T> {
    private final String key;
    private final TextObject description;
    private final T defaultValue;
    private T value;
    private String comment;

    public ConfigEntry(String key, T value, TextObject description) {
        this.key = transform(key);
        this.defaultValue = value;
        this.value = value;
        this.description = description;
    }

    private String transform(String key) {
        String[] split = key.split("\\.");
        for (int i = 0; i < split.length; i++) {
            split[i] = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, split[i]);
        }

        return String.join(".", split);
    }

    public T get() {
        return this.value;
    }

    public void set(@NotNull T value) {
        Preconditions.checkNotNull(value, "Entry value shouldn't be null.");
        this.value = value;
    }

    public ConfigEntry<T> comment(String comment) {
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

    public String getComment() {
        return this.comment;
    }

    public String getKey() {
        return this.key;
    }

    public String write() {
        return this.value.toString();
    }

    public TextObject getDescription() {
        return this.description;
    }

    public Widget createButton(Config options, int x, int y, int width) {
        return new Widget(width, 20) {
            @Override
            public Widget position(Supplier<Position> position) {
                this.pos.set(x, y);
                return this;
            }

            @Override
            public Widget bounds(Supplier<Bounds> position) {
                this.bounds.set(x, y, width, 20);
                return this;
            }

            @Override
            public void renderWidget(@NotNull Renderer gfx, int i, int j, float f) {
                gfx.textCenter(ConfigEntry.this.getDescription(), this.getX() + (float) this.getWidth() / 2, this.getY() + (this.getHeight() / 2 - 5), 0xffffffff);
            }
        };
    }

    public abstract void setFromWidget(Widget widget);

    public void reset() {
        this.value = this.defaultValue;
    }

    public T getDefault() {
        return defaultValue;
    }
}
