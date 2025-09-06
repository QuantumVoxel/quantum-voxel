package dev.ultreon.hydro.util;

public abstract class FlushablePool<T> extends Pool<T> {
    public void flush() {
        objects.addAll(objectsInUse);
        objectsInUse.clear();
    }
}
