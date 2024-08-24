package dev.ultreon.quantum.api.events.world;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.world.WorldAccess;
import org.jetbrains.annotations.NotNull;

public abstract class WorldAccessEvent extends Event {
    private final @NotNull WorldAccess world;

    public WorldAccessEvent(@NotNull WorldAccess world) {
        this.world = world;
    }

    public @NotNull WorldAccess getWorld() {
        return this.world;
    }
}
