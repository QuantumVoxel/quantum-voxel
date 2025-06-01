package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryHandle;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocalRegistries implements RegistryHandle {
    private final Map<NamespaceID, ExternalRegistry<?>> registries = new HashMap<>();

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> ExternalRegistry<T> get(RegistryKey<? extends Registry<T>> registryKey) {
        return (ExternalRegistry<T>) registries.computeIfAbsent(registryKey.id(), id -> new ExternalRegistry<>(registryKey));
    }

    public <T> void set(RegistryKey<ExternalRegistry<T>> registryKey, ExternalRegistry<T> registry) {
        registries.put(registryKey.id(), registry);
    }

    public ExternalRegistry<?> get(NamespaceID registryID) {
        return registries.get(registryID);
    }

    public Set<NamespaceID> ids() {
        return registries.keySet();
    }
}
