package dev.ultreon.quantum.client.config.entries;

import dev.ultreon.quantum.client.config.gui.ConfigEntry;
import dev.ultreon.quantum.client.gui.widget.CycleButton;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.text.TextObject;

public class EnumEntry<T extends Enum<T>> extends ConfigEntry<T> {
    public EnumEntry(String key, T enumValue, CraftyConfig config) {
        super(key, enumValue, config);
    }

    @Override
    protected T read(String text) {
        return Enum.valueOf(this.getDefaultValue().getDeclaringClass(), text);
    }

    @Override
    public String write() {
        return this.get().name();
    }

    @Override
    public Widget createWidget() {
        return new CycleButton<T>()
                .label("Enum")
                .value(this.get())
                .values(this.getDefaultValue().getDeclaringClass().getEnumConstants())
                .formatter(value -> TextObject.literal(value.name()))
                .setCallback(this::set);
    }

    private void set(CycleButton<T> tCycleButton) {
        this.set(tCycleButton.getValue());
    }
}
