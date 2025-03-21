package dev.ultreon.quantum.registry;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.*;

/**
 * Base registry.
 *
 * @param <K> The type key type for the registry.
 * @param <V> The type value type for the registry.
 */
public abstract class AbstractRegistryMap<K, V> {
    protected final HashMap<K, V> registry = new HashMap<>();
    private final Array<V> temp = new Array<>();

    protected AbstractRegistryMap() throws IllegalStateException {

    }

    public abstract V get(K obj);

    public abstract void register(K key, V val);

    public abstract ObjectMap.Values<V> values();

    public abstract ObjectMap.Keys<K> keys();

    public abstract ObjectMap.Entries<K, V> entries() throws IllegalAccessException;

    @Deprecated
    public V random() {
        return this.random(new Random());
    }

    @Deprecated
    private V random(Random random) {
        temp.clear();
        return this.values().toArray(temp).get(random.nextInt(this.size()));
    }

    protected abstract int size();

    public boolean isEmpty() {
        return this.size() == 0;
    }
}
