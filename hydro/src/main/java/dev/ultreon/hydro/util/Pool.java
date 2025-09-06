package dev.ultreon.hydro.util;

import java.util.ArrayList;
import java.util.List;

public abstract class Pool<T> {
    protected final List<T> objects = new ArrayList<>();
    protected final List<T> objectsInUse = new ArrayList<>();

    public Pool() {

    }

    public T obtain() {
        if(objects.isEmpty()) {
            return create();
        }

        T remove = objects.remove(objects.size() - 1);
        objectsInUse.add(remove);
        return remove;
    }

    public void free(T object) {
        boolean remove = objectsInUse.remove(object);
        if(!remove) {
            throw new IllegalStateException("Object is not in use!");
        }
        objects.add(object);
    }

    public abstract T create();

    public abstract void destroy(T object);

    public void clear() {
        for(T object : objects) {
            destroy(object);
        }

        objects.clear();
        objectsInUse.clear();
    }
}
