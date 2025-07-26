package dev.ultreon.quantum.world;

import java.util.function.Supplier;

public class Timing {
    public static long measureTimeMillis(Supplier<?> runnable) {
        long start = System.currentTimeMillis();
        runnable.get();
        return System.currentTimeMillis() - start;
    }
}
