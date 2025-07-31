package dev.ultreon.quantum.registry;

import dev.ultreon.quantum.api.events.ModEvent;
import org.jetbrains.annotations.NotNull;

public class AutoRegisterEvent implements ModEvent {
    private final String modId;
    private final Registry<?> registry;

    public AutoRegisterEvent(String modId, Registry<?> registry) {
        this.modId = modId;
        this.registry = registry;
    }

    @Override
    public @NotNull String getModId() {
        return this.modId;
    }

    public Registry<?> getRegistry() {
        return this.registry;
    }
}
