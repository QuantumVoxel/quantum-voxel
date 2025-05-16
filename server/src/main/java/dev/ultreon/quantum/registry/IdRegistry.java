package dev.ultreon.quantum.registry;

public interface IdRegistry<T> {
    T byRawId(int id);

    int getRawId(T object);

    RegistryKey<T> nameById(int i);

    int idByName(RegistryKey<T> biome);
}
