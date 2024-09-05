package dev.ultreon.quantum.registry;

import com.mojang.serialization.Codec;
import dev.ultreon.quantum.util.NamespaceID;

public record RegistryKey<T>(dev.ultreon.quantum.registry.RegistryKey<Registry<T>> parent, NamespaceID id) {
    public static final RegistryKey<Registry<Registry<?>>> ROOT = new RegistryKey<>(null, new NamespaceID("root"));
    public static final Codec<RegistryKey<Registry<?>>> REGISTRY_KEY_CODEC = NamespaceID.CODEC.xmap(RegistryKey::registry, RegistryKey::id);

    public static <T> RegistryKey<T> of(RegistryKey<Registry<T>> parent, NamespaceID element) {
        if (element == null) throw new IllegalArgumentException("Element ID cannot be null");
        return new RegistryKey<>(parent, element);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Registry<?>> RegistryKey<T> registry(T registry) {
        return (RegistryKey<T>) new RegistryKey<>(ROOT, registry.id());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Registry<?>> RegistryKey<T> registry(NamespaceID id) {
        return (RegistryKey<T>) new RegistryKey<>(ROOT, id);
    }

    @Override
    public String toString() {
        if (parent == null) return id.toString();
        return parent.id + " @ " + id;
    }
}
