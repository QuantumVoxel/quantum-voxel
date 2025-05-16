package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryHandle;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocalRegistries implements RegistryHandle {
    private final Map<NamespaceID, SyncedRegistry<?>> registries = new HashMap<>();

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> SyncedRegistry<T> get(RegistryKey<? extends Registry<T>> registryKey) {
        return (SyncedRegistry<T>) registries.computeIfAbsent(registryKey.id(), id -> new SyncedRegistry<>(registryKey));
    }

    public <T> void set(RegistryKey<SyncedRegistry<T>> registryKey, SyncedRegistry<T> registry) {
        registries.put(registryKey.id(), registry);
    }

    public SyncedRegistry<?> get(NamespaceID registryID) {
        return registries.get(registryID);
    }

    public Set<NamespaceID> ids() {
        return registries.keySet();
    }
}
