package dev.ultreon.quantum.event;

/**
 * Interface for event handlers.
 * Event handlers are responsible for handling events of a specific type.
 *
 * @param <T> the type of event to handle
 */
@FunctionalInterface
public interface EventHandler<T extends Event> {
    /**
     * Handles an event.
     *
     * @param event the event to handle
     */
    void handle(T event);
    
    /**
     * Gets the priority of the event handler.
     * Higher priority handlers are called first.
     *
     * @return the priority
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Whether to ignore canceled events.
     * If true, the handler will not be called for events that have been canceled.
     *
     * @return true if canceled events should be ignored
     */
    default boolean ignoreCanceled() {
        return false;
    }
    
    /**
     * Compares this event handler to another based on priority.
     * Higher priority handlers come first.
     *
     * @param other the other event handler
     * @return a negative integer, zero, or a positive integer as this handler has higher, equal, or lower priority than the other
     */
    default int compareTo(EventHandler<?> other) {
        return Integer.compare(other.getPriority(), this.getPriority());
    }
}