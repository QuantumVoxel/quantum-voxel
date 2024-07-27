package dev.ultreon.quantum.client.config.entries;

import dev.ultreon.quantum.client.config.gui.ConfigEntry;
import dev.ultreon.quantum.client.gui.widget.TextEntry;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.config.crafty.CraftyConfig;

import java.util.UUID;

public class UUIDEntry extends ConfigEntry<UUID> {
    public UUIDEntry(String key, UUID value, CraftyConfig config) {
        super(key, value, config);
    }

    @Override
    protected UUID read(String text) {
        return UUID.fromString(text);
    }

    @Override
    public Widget createWidget() {
        return TextEntry.of(value.toString())
                .callback(this::set);
    }

    private void set(TextEntry textEntry) {
        try {
            this.value = UUID.fromString(textEntry.getValue());
        } catch (Exception ignored) {
        }
    }

}
