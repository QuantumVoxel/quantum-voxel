package dev.ultreon.quantum.api.events.world;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.world.WorldAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WorldAccessEvent extends Event {
    @Nullable WorldAccess getWorld();
}
