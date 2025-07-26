package dev.ultreon.mixinprovider;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.bidimap.DualLinkedHashBidiMap;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class GdxRegistry<T> implements Iterable<T> {
    private final BidiMap<Integer, T> map = new DualLinkedHashBidiMap<>();
    private int id;

    private void register(int id, @NotNull T object) {
        if (map.containsKey(id)) {
            return;
        }
        map.put(id, object);
    }

    public int getId(@NotNull T object) {
        Integer key = map.getKey(object);
        if (key == null) {
            return -1;
        }
        return key;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return map.values().iterator();
    }

    public void register(T model) {
        register(nextId(), model);
    }

    private int nextId() {
        return id++;
    }

    public void unregister(T model) {
        map.removeValue(model);
    }
}
