package dev.ultreon.quantum.api.events.world;

import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.Nullable;

public interface WorldEvent extends WorldAccessEvent {
    @Override
    @Nullable
    World getWorld();
}
