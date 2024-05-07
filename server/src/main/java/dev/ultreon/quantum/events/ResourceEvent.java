package dev.ultreon.quantum.events;

import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.resources.ResourcePackage;

public class ResourceEvent {
    public static final Event<PackageImported> IMPORTED = Event.create();

    @FunctionalInterface
    public interface PackageImported {
        void onImported(ResourcePackage pkg);
    }
}
