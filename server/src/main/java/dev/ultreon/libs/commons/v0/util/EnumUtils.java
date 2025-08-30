package dev.ultreon.libs.commons.v0.util;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class EnumUtils {
    private EnumUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <E extends Enum<E>> E byIndex(int value, E defaultValue, Function<E, Integer> getter) {
        for (E e : defaultValue.getDeclaringClass().getEnumConstants()) {
            if (getter.apply(e) == value) {
                return e;
            }
        }
        return defaultValue;
    }

    public static <E extends Enum<E>> E byName(String name, E defaultValue) {
        for (E e : defaultValue.getDeclaringClass().getEnumConstants()) {
            if (e.name().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return defaultValue;
    }

    public static <E extends Enum<E>> E byOrdinal(int ordinal, E defaultValue) {
        E[] enumConstants = defaultValue.getDeclaringClass().getEnumConstants();
        if (ordinal >= 0 && ordinal < enumConstants.length) {
            return enumConstants[ordinal];
        }
        return defaultValue;
    }

    public static <E extends Enum<E>> boolean validate(@Nullable Object obj, Class<E> enumClass) {
        if (obj != null) {
            for (E e : enumClass.getEnumConstants()) {
                if (e.name().equalsIgnoreCase(obj.toString())) {
                    return true;
                }
            }
        }
        return false;
    }
}
