package dev.ultreon.langgen.api;

import java.lang.reflect.Parameter;

public interface MethodVisitor extends TypedVisitor {
    String visitParameterList(Parameter[] parameters);

    String visitParameter(Parameter parameter);
}
