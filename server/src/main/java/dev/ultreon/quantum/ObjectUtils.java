package dev.ultreon.quantum;

import java.util.function.Supplier;

public class ObjectUtils {
//    requireNonNullElse()
    private ObjectUtils() { }

    public static <T> T requireNonNullElse(T obj, T defaultObj) {
        return obj != null ? obj : defaultObj;
    }

    public static <T> T requireNonNullElseGet(T obj, Supplier<T> defaultSupplier) {
        return obj != null ? obj : defaultSupplier.get();
    }
}
