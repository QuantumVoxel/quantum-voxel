package dev.ultreon.xeox.compat.mixin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Sets {
    @SafeVarargs
    public static <T> Set<T> newHashSet(T... s) {
        Set<T> set = new HashSet<>();
        Collections.addAll(set, s);
        return set;
    }
}
