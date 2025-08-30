package dev.ultreon.quantum;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SetUtils {
    private SetUtils() {}

    public static <T> java.util.Set<T> of(T... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    public static <T> java.util.Set<T> of(java.util.Set<T> set, T... elements) {
        set.addAll(Arrays.asList(elements));
        return set;
    }
}
