package dev.ultreon.langgen.api;

import java.lang.reflect.TypeVariable;

public interface TypedVisitor {
    String visitTypeVariables(TypeVariable<? extends Class<?>>[] typeVariables);

    String visitTypeVariable(TypeVariable<? extends Class<?>> typeVariable);
}
