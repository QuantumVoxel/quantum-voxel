package dev.ultreon.langgen.api;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;

public interface ClassBuilder {
    static boolean isInvisible(Member method) {
        return !Modifier.isProtected(method.getModifiers()) && !Modifier.isPublic(method.getModifiers());
    }

    static boolean isInvisible(Class<?> method) {
        return !Modifier.isProtected(method.getModifiers()) && !Modifier.isPublic(method.getModifiers());
    }

    @Nullable List<String> build(StringBuilder sw, Path output);

    @Nullable
    String convertImport(Class<?> clazz, Class<?> type, String java, String python);
}
