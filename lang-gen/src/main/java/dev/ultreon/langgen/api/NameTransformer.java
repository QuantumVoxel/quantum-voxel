package dev.ultreon.langgen.api;

@FunctionalInterface
public interface NameTransformer {
    String transformName(Class<?> clazz);
}
