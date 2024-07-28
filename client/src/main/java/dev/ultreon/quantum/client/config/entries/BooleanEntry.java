package dev.ultreon.quantum.client.config.entries;

import dev.ultreon.quantum.client.config.gui.ConfigEntry;
import dev.ultreon.quantum.client.gui.widget.CycleButton;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.text.TextObject;

public class BooleanEntry extends ConfigEntry<Boolean> {
    public BooleanEntry(String key, boolean value, CraftyConfig config) {
        super(key, value, config);
    }

    @Override
    protected Boolean read(String text) {
        return Boolean.parseBoolean(text);
    }

    @Override
    public Widget createWidget() {
        return new CycleButton<Boolean>()
                .label("Toggle")
                .values(Boolean.FALSE, Boolean.TRUE)
                .value(this.value)
                .formatter(value -> TextObject.nullToEmpty(value ? "on" : "off"))
                .callback((value) -> {
                    this.set(value.getValue());
                });
    }

}
