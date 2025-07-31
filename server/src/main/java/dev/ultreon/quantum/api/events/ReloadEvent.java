package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.resources.ReloadContext;

public interface ReloadEvent {
    ReloadContext getReloadContext();
}
