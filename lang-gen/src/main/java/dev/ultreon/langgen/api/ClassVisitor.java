package dev.ultreon.langgen.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface ClassVisitor extends TypedVisitor {
    String visitMethod(Method method, MethodVisitor visitor);

    String visitField(Field field);
}
