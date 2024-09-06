package dev.ultreon.quantum.registry.event;

import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.registry.Registry;

public class RegistryEvents {
    public static final Event<RegistryDump> REGISTRY_DUMP = Event.create();
    public static final Event<AutoRegister> AUTO_REGISTER = Event.create();
    public static final Event<RegistryCreation> REGISTRY_CREATION = Event.create();

    @FunctionalInterface
    public interface RegistryDump {
        void onRegistryDump();
    }

    @FunctionalInterface
    public interface AutoRegister {
        void onAutoRegister(String modId, Registry<?> registry);
    }

    @FunctionalInterface
    public interface RegistryCreation {
        void onRegistryCreation();
    }
}
