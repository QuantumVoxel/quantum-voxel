package dev.ultreon.quantum;

import java.util.*;
import java.util.function.Predicate;

public class ListUtils {
    private ListUtils() { }

    public static <T> List<T> of(T... elements) {
        return Arrays.asList(elements);
    }

    public static <T> List<T> of(List<T> list, T... elements) {
        list.addAll(Arrays.asList(elements));
        return list;
    }

    @SuppressWarnings("Java8CollectionRemoveIf")
    public static <T> void removeIf(Collection<T> notifications, Predicate<T> predicate) {
        Iterator<T> iterator = notifications.iterator();
        while (iterator.hasNext()) {
            T notification = iterator.next();
            if (predicate.test(notification)) {
                iterator.remove();
            }
        }
    }
}
