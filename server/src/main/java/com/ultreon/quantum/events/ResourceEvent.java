package com.ultreon.quantum.events;

import com.ultreon.quantum.events.api.Event;
import com.ultreon.quantum.resources.ResourcePackage;

public class ResourceEvent {
    public static final Event<PackageImported> IMPORTED = Event.create();

    @FunctionalInterface
    public interface PackageImported {
        void onImported(ResourcePackage pkg);
    }
}
