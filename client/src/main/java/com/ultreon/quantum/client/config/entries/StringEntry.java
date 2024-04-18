package com.ultreon.quantum.client.config.entries;

import com.ultreon.quantum.client.config.Configuration;
import com.ultreon.quantum.client.config.gui.ConfigEntry;

public class StringEntry extends ConfigEntry<String> {
    public StringEntry(String key, String value, Configuration config) {
        super(key, value, config);
    }

    @Override
    protected String read(String text) {
        return text;
    }

}
