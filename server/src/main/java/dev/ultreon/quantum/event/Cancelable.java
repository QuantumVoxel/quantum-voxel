package dev.ultreon.quantum.event;

/**
 * Interface for events that can be canceled.
 * Events that implement this interface can be canceled by event handlers.
 */
public interface Cancelable {
    /**
     * Checks if the event is canceled.
     *
     * @return true if the event is canceled
     */
    boolean isCanceled();
    
    /**
     * Sets whether the event is canceled.
     *
     * @param canceled true to cancel the event
     */
    void setCanceled(boolean canceled);
}