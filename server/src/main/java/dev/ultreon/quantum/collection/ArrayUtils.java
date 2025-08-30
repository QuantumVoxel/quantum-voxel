package dev.ultreon.quantum.collection;

import dev.ultreon.quantum.util.Range;

import java.lang.reflect.Method;

public class ArrayUtils {
    public static boolean contains(short[] arr, short v) {
        for (short s : arr) {
            if (s == v) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(int[] arr, int v) {
        for (int i : arr) {
            if (i == v) {
                return true;
            }
        }
        return false;
    }

    public static String[] remove(String[] arr, int idx) {
        String[] result = new String[arr.length - 1];
        System.arraycopy(arr, 0, result, 0, idx);
        System.arraycopy(arr, idx + 1, result, idx, arr.length - idx - 1);
        return result;
    }

    public static <T> int indexOf(T[] values, T o) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(o)) {
                return (byte) i;
            }
        }
        return -1;
    }

    public static Range[] add(Range[] ranges, Range integers) {
        Range[] result = new Range[ranges.length + 1];
        System.arraycopy(ranges, 0, result, 0, ranges.length);
        result[ranges.length] = integers;
        return result;
    }

    public static boolean contains(Method[] methods, Method method) {
        for (Method m : methods) {
            if (m.equals(method)) {
                return true;
            }
        }
        return false;
    }
}
