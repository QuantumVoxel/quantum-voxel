package dev.ultreon.quantum.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for event handler methods.
 * Methods annotated with this annotation will be automatically registered as event handlers.
 * The method must have exactly one parameter, which is the event type to handle.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {
    /**
     * The priority of the event handler.
     * Higher priority handlers are called first.
     *
     * @return the priority
     */
    int priority() default 0;
    
    /**
     * Whether to ignore canceled events.
     * If true, the handler will not be called for events that have been canceled.
     *
     * @return true if canceled events should be ignored
     */
    boolean ignoreCanceled() default false;
}