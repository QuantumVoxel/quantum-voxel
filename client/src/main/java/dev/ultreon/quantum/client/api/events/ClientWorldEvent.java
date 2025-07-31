package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.api.events.world.WorldEvent;
import dev.ultreon.quantum.client.world.ClientWorld;

public interface ClientWorldEvent extends WorldEvent, ClientWorldAccessEvent {
    @Override
    ClientWorld getWorld();
}
