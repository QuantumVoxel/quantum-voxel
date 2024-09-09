package dev.ultreon.langgen.api;

import com.badlogic.gdx.utils.ObjectMap;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PackageExclusions {
    private static Set<String> packageNames = new HashSet<>();

    public static void addExclusion(String packageName) {
        if (packageName.endsWith("."))
            throw new IllegalArgumentException("Cannot end with period.");

        packageNames.add(packageName);
    }

    public static boolean isExcluded(Class<?> type) {
        if (type == null) return true;
        if (type.getPackageName().equals("java.lang.reflect")) return false;
        if (type == String.class) return false;
        if (type == Class.class) return false;
        if (type == ObjectMap.class
            || type == ObjectMap.Entry.class
            || type == ObjectMap.Keys.class
            || type == ObjectMap.Values.class
            || type == ObjectMap.Entries.class
        ) return true;
        if (!(Modifier.isPublic(type.getModifiers())
              || Modifier.isProtected(type.getModifiers()))) return true;

        for (String name : packageNames) {
            if (type.getName().startsWith(name + "."))
                return true;
        }

        return false;
    }
}
