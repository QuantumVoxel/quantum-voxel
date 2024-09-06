package dev.ultreon.quantum.util;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
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

    public static <T> Supplier<T> memoizeWithExpiration(Supplier<T> supplier, int time, TimeUnit unit) {
        return new Supplier<>() {
            private T value;
            private boolean initialized;
            private final Instant expiration = Instant.now().plus(time, unit.toChronoUnit());

            @Override
            public synchronized T get() {
                if (!initialized || Instant.now().isAfter(expiration)) {
                    value = supplier.get();
                    initialized = true;
                }

                return value;
            }
        };
    }
}
