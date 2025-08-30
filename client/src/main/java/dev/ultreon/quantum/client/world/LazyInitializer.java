package dev.ultreon.quantum.client.world;

import java.util.function.Supplier;

public class LazyInitializer<T> {
    private final Supplier<T> supplier;
    private T instance;
    private boolean initialized = false;

    public LazyInitializer(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (!initialized) {
            instance = supplier.get();
            initialized = true;
        }
        return instance;
    }
}
