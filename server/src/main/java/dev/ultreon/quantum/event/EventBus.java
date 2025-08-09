package dev.ultreon.quantum.event;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for event buses.
 * Event buses are responsible for dispatching events to registered handlers.
 */
public interface EventBus {
    /**
     * Posts an event to the event bus.
     * This method dispatches the event to all registered handlers.
     *
     * @param event the event to post
     * @param <T> the type of the event
     * @return true if the event was canceled, false otherwise
     */
    <T extends Event> boolean post(T event);
    
    /**
     * Posts an event to the event bus asynchronously.
     * This method dispatches the event to all registered handlers in a separate thread.
     *
     * @param event the event to post
     * @param <T> the type of the event
     * @return a future that completes when the event has been processed
     */
    <T extends Event> CompletableFuture<Boolean> postAsync(T event);
    
    /**
     * Registers all event handlers in the given object.
     * This method scans the object for methods annotated with {@link Subscribe}
     * and registers them as event handlers.
     *
     * @param object the object to register
     */
    void register(Object object);
    
    /**
     * Unregisters all event handlers in the given object.
     * This method removes all event handlers that were registered for the given object.
     *
     * @param object the object to unregister
     */
    void unregister(Object object);
    
    /**
     * Registers a specific event handler.
     * This method registers the given handler for the specified event type.
     *
     * @param eventType the type of event to handle
     * @param handler the handler to register
     * @param <T> the type of the event
     */
    <T extends Event> void register(Class<T> eventType, EventHandler<T> handler);
    
    /**
     * Unregisters a specific event handler.
     * This method removes the given handler for the specified event type.
     *
     * @param eventType the type of event to handle
     * @param handler the handler to unregister
     * @param <T> the type of the event
     */
    <T extends Event> void unregister(Class<T> eventType, EventHandler<T> handler);
    
    /**
     * Checks if the event bus has any handlers registered for the given event type.
     *
     * @param eventType the type of event to check
     * @return true if there are handlers registered for the event type
     */
    boolean hasHandlers(Class<? extends Event> eventType);
    
    /**
     * Clears all registered handlers.
     * This method removes all event handlers from the event bus.
     */
    void clear();
}