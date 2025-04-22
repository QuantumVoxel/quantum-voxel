package dev.ultreon.quantum.util;

import java.util.function.Supplier;

public class Suppliers {
    private Suppliers() {

    }

    public static <T> Supplier<T> memoize(Supplier<T> supplier) {
        return new Supplier<>() {
            private T value;
            private boolean initialized;

            @Override
            public synchronized T get() {
                if (!initialized) {
                    value = supplier.get();
                    initialized = true;
                }

                return value;
            }
        };
    }
}
