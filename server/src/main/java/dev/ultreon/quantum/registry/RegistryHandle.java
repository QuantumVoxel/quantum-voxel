package dev.ultreon.quantum.registry;

public interface RegistryHandle {
    <T> IdRegistry<T> get(RegistryKey<? extends Registry<T>> key);
}
