package dev.ultreon.quantum.event;

import java.util.concurrent.CompletableFuture;

/**
 * Utility class for working with events.
 * This class provides static methods for posting events and registering event handlers.
 */
public final class Events {
    private static final EventBus DEFAULT_BUS = new DefaultEventBus();
    
    // Private constructor to prevent instantiation
    private Events() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
    
    /**
     * Gets the default event bus.
     *
     * @return the default event bus
     */
    public static EventBus getBus() {
        return DEFAULT_BUS;
    }
    
    /**
     * Posts an event to the default event bus.
     *
     * @param event the event to post
     * @param <T> the type of the event
     * @return true if the event was canceled, false otherwise
     */
    public static <T extends Event> boolean post(T event) {
        return DEFAULT_BUS.post(event);
    }
    
    /**
     * Posts an event to the default event bus asynchronously.
     *
     * @param event the event to post
     * @param <T> the type of the event
     * @return a future that completes when the event has been processed
     */
    public static <T extends Event> CompletableFuture<Boolean> postAsync(T event) {
        return DEFAULT_BUS.postAsync(event);
    }
    
    /**
     * Registers all event handlers in the given object with the default event bus.
     *
     * @param object the object to register
     */
    public static void register(Object object) {
        DEFAULT_BUS.register(object);
    }
    
    /**
     * Unregisters all event handlers in the given object from the default event bus.
     *
     * @param object the object to unregister
     */
    public static void unregister(Object object) {
        DEFAULT_BUS.unregister(object);
    }
    
    /**
     * Registers a specific event handler with the default event bus.
     *
     * @param eventType the type of event to handle
     * @param handler the handler to register
     * @param <T> the type of the event
     */
    public static <T extends Event> void register(Class<T> eventType, EventHandler<T> handler) {
        DEFAULT_BUS.register(eventType, handler);
    }
    
    /**
     * Unregisters a specific event handler from the default event bus.
     *
     * @param eventType the type of event to handle
     * @param handler the handler to unregister
     * @param <T> the type of the event
     */
    public static <T extends Event> void unregister(Class<T> eventType, EventHandler<T> handler) {
        DEFAULT_BUS.unregister(eventType, handler);
    }
    
    /**
     * Checks if the default event bus has any handlers registered for the given event type.
     *
     * @param eventType the type of event to check
     * @return true if there are handlers registered for the event type
     */
    public static boolean hasHandlers(Class<? extends Event> eventType) {
        return DEFAULT_BUS.hasHandlers(eventType);
    }
    
    /**
     * Clears all registered handlers from the default event bus.
     */
    public static void clear() {
        DEFAULT_BUS.clear();
    }
}