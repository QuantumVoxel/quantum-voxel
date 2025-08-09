package dev.ultreon.quantum.di;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field or constructor for dependency injection.
 * Fields or constructors marked with this annotation will be automatically
 * injected with dependencies from the {@link DependencyContainer}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface Inject {
}