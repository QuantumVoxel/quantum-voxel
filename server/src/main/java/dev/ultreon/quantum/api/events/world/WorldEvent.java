package dev.ultreon.quantum.api.events.world;

import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;

public abstract class WorldEvent extends WorldAccessEvent {
    public WorldEvent(World world) {
        super(world);
    }

    @Override
    public @NotNull World getWorld() {
        return (World) super.getWorld();
    }
}
