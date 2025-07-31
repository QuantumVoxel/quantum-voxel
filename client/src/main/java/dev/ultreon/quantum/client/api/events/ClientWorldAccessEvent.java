package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.api.events.world.WorldAccessEvent;
import dev.ultreon.quantum.client.world.ClientWorldAccess;

public interface ClientWorldAccessEvent extends WorldAccessEvent, ClientEvent {
    @Override
    ClientWorldAccess getWorld();
}
