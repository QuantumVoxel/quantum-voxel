package dev.ultreon.langgen.javascript.ts;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.function.Function;

public class TSConstructor extends TSArgumentStruct<Constructor<?>> {
    private static final String CONTENT = "/* Stub */";
    @Nullable
    private Class<?> declaringClass;

    public TSConstructor(String name) {
        super(name);
    }

    @Override
    public void addStruct(Constructor<?> member, Parameter[] content, Function<Class<?>, String> converter) {
        declaringClass = member.getDeclaringClass();
        super.addStruct(member, content, converter);
    }

    @Override
    public String getTypeName() {
        return "DynConstruct";
    }

    @Override
    public String toString() {
        if (structNames.isEmpty()) {
            return "constructor() { " + CONTENT.trim() + " }";
        }
        Class<?> superclass = declaringClass == null ? null : declaringClass.getSuperclass();
        Class<?>[] interfaces = declaringClass == null ? null : declaringClass.getInterfaces();
        if (interfaces != null && structNames.isEmpty() && (superclass == null || superclass == Object.class) && interfaces.length == 0) {
            return "constructor() { " + CONTENT.trim() + " }";
        }

        return "constructor(...argv: " + String.join("|", structNames) + ") { " + CONTENT.trim() + " }";
    }
}
