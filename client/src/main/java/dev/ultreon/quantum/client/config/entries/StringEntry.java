package dev.ultreon.quantum.client.config.entries;

import dev.ultreon.quantum.client.config.gui.ConfigEntry;
import dev.ultreon.quantum.client.gui.widget.TextEntry;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.config.crafty.CraftyConfig;

public class StringEntry extends ConfigEntry<String> {
    public StringEntry(String key, String value, CraftyConfig config) {
        super(key, value, config);
    }

    @Override
    protected String read(String text) {
        return text;
    }

    @Override
    public Widget createWidget() {
        return TextEntry.of(this.value)
                .withCallback(this::onValueChanged);
    }

    private void onValueChanged(TextEntry textEntry) {
        this.value = textEntry.getValue();
        this.config.save();
    }

}
