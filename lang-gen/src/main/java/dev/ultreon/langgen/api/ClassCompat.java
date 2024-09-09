package dev.ultreon.langgen.api;

import org.intellij.lang.annotations.Language;

import java.util.HashSet;
import java.util.Set;

public class ClassCompat {
    private static final Set<Class<?>> FORCES_ABSTRACT_CLASS = new HashSet<>();

    public static void forceAbstractClass(@Language("jvm-class-name") String classname) {
        try {
            FORCES_ABSTRACT_CLASS.add(Class.forName(classname, false, ClassCompat.class.getClassLoader()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isForcedAbstract(Class<?> clazz) {
        return FORCES_ABSTRACT_CLASS.contains(clazz);
    }
}
