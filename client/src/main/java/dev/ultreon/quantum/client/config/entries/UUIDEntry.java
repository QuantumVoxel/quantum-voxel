package dev.ultreon.quantum.client.config.entries;

import dev.ultreon.quantum.client.config.Configuration;
import dev.ultreon.quantum.client.config.gui.ConfigEntry;

import java.util.UUID;

public class UUIDEntry extends ConfigEntry<UUID> {
    public UUIDEntry(String key, UUID value, Configuration config) {
        super(key, value, config);
    }

    @Override
    protected UUID read(String text) {
        return UUID.fromString(text);
    }

}
