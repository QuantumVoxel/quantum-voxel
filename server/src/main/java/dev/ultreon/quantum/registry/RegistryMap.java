package dev.ultreon.quantum.registry;

import com.badlogic.gdx.utils.ObjectMap;

/**
 * Base registry.
 *
 * @param <K> The type key type for the registry.
 * @param <V> The type value type for the registry.
 */
public interface RegistryMap<K, V> {
    V get(K obj);
    void register(K key, V val);

    int size();

    ObjectMap.Entries<K, V> entries() throws IllegalAccessException;

    ObjectMap.Keys<K> keys();

    ObjectMap.Values<V> values();

    default boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * Returns the key relevant to the raw ID
     *
     * @param rawID the raw ID
     * @return the key relevant to the raw ID.
     * @throws UnsupportedOperationException if nameById is not supported
     */
    RegistryKey<V> nameById(int rawID);
}
