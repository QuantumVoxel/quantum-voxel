package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.api.event.Event;

public interface ModEvent extends Event {
    String getModId();
}
