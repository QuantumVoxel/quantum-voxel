package dev.ultreon.quantum.event;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Wrapper for event handlers.
 * This class wraps an event handler and provides additional information about it.
 *
 * @param <T> the type of event handled by the handler
 */
class EventHandlerWrapper<T extends Event> {
    private final EventHandler<T> handler;
    private final Object owner;
    private final Method method;
    private final int priority;
    private final boolean ignoreCanceled;
    
    /**
     * Creates a new event handler wrapper for a method.
     *
     * @param owner the object that owns the method
     * @param method the method to wrap
     * @param priority the priority of the handler
     * @param ignoreCanceled whether to ignore canceled events
     */
    EventHandlerWrapper(Object owner, Method method, int priority, boolean ignoreCanceled) {
        this.owner = owner;
        this.method = method;
        this.priority = priority;
        this.ignoreCanceled = ignoreCanceled;
        
        // Create a handler that invokes the method
        this.handler = event -> {
            try {
                method.setAccessible(true);
                method.invoke(owner, event);
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke event handler method: " + method, e);
            }
        };
    }
    
    /**
     * Creates a new event handler wrapper for an existing handler.
     *
     * @param handler the handler to wrap
     * @param owner the object that owns the handler
     */
    EventHandlerWrapper(EventHandler<T> handler, Object owner) {
        this.handler = handler;
        this.owner = owner;
        this.method = null;
        this.priority = handler.getPriority();
        this.ignoreCanceled = handler.ignoreCanceled();
    }
    
    /**
     * Gets the event handler.
     *
     * @return the event handler
     */
    public EventHandler<T> getHandler() {
        return handler;
    }
    
    /**
     * Gets the owner of the handler.
     *
     * @return the owner
     */
    public Object getOwner() {
        return owner;
    }
    
    /**
     * Gets the method that is wrapped by this handler.
     *
     * @return the method, or null if this wrapper doesn't wrap a method
     */
    public Method getMethod() {
        return method;
    }
    
    /**
     * Gets the priority of the handler.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Checks if the handler ignores canceled events.
     *
     * @return true if the handler ignores canceled events
     */
    public boolean isIgnoreCanceled() {
        return ignoreCanceled;
    }
    
    /**
     * Handles an event.
     *
     * @param event the event to handle
     */
    public void handle(T event) {
        // Skip if the event is canceled and we should ignore canceled events
        if (event.isCanceled() && ignoreCanceled) {
            return;
        }
        
        // Handle the event
        handler.handle(event);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventHandlerWrapper<?> that = (EventHandlerWrapper<?>) o;
        return Objects.equals(owner, that.owner) &&
                Objects.equals(method, that.method) &&
                Objects.equals(handler, that.handler);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(owner, method, handler);
    }
}