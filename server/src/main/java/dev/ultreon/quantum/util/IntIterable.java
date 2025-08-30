package dev.ultreon.quantum.util;

import org.jetbrains.annotations.NotNull;

public interface IntIterable extends Iterable<Integer> {
    IntIterator intIterator();

    @Override
    default @NotNull IntIterator iterator() {
        return intIterator();
    }

    static interface IntIterator extends java.util.PrimitiveIterator.OfInt {
        @Override
        int nextInt();

        @Override
        default Integer next() {
            return nextInt();
        }
    }
}
