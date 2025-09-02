package dev.ultreon.quantum.registry;

import dev.ultreon.quantum.util.NamespaceID;

public interface IdRegistry<T> {
    T byRawId(int id);

    int getRawId(T object);

    RegistryKey<T> nameById(int i);

    int idByName(RegistryKey<T> biome);

    T get(NamespaceID from);
}
