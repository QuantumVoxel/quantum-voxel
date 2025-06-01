package dev.ultreon.quantum.client.registry;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import dev.ultreon.quantum.registry.IdRegistry;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.exception.RegistryException;
import dev.ultreon.quantum.util.NamespaceID;

/**
 * Represents the client registry.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class ExternalRegistry<T> implements IdRegistry<T> {
    private final IntMap<NamespaceID> idMap = new IntMap<>();
    private final RegistryKey<? extends Registry<T>> key;

    /**
     * Constructs a new client registry.
     *
     * @param key the key.
     * @throws IllegalStateException if the registry is already registered.
     */
    public ExternalRegistry(RegistryKey<? extends Registry<T>> key) throws IllegalStateException {
        this.key = key;
    }

    public T byRawId(int id) {
        throw new UnsupportedOperationException("Objects not available in synced registries!");
    }

    @Override
    public int getRawId(T object) {
        throw new UnsupportedOperationException("Objects not available in synced registries!");
    }

    public RegistryKey<T> nameById(int i) {
        return RegistryKey.of(key, idMap.get(i));
    }

    @Override
    public int idByName(RegistryKey<T> biome) {
        int rawId = idMap.findKey(biome, false, -1);
        if (rawId == -1) throw new RegistryException("Missing key: " + key);
        return rawId;
    }

    public Array<NamespaceID> ids() {
        throw new UnsupportedOperationException("Objects not available in synced registries!");
    }

    public void load(IntMap<NamespaceID> registryMap) {
        unload();
        idMap.putAll(registryMap);
    }

    public void unload() {
        idMap.clear();
    }

    public RegistryKey<? extends Registry<T>> key() {
        return key;
    }
}
