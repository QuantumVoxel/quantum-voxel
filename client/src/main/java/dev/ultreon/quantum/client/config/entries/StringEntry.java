package dev.ultreon.quantum.client.config.entries;

import dev.ultreon.quantum.client.config.Configuration;
import dev.ultreon.quantum.client.config.gui.ConfigEntry;

public class StringEntry extends ConfigEntry<String> {
    public StringEntry(String key, String value, Configuration config) {
        super(key, value, config);
    }

    @Override
    protected String read(String text) {
        return text;
    }

}
