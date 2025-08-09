package dev.ultreon.quantum.event;

/**
 * Base class for all events.
 * Events are used to notify listeners about specific occurrences in the game.
 */
public abstract class Event {
    private boolean canceled = false;
    
    /**
     * Checks if the event is cancelable.
     *
     * @return true if the event is cancelable
     */
    public boolean isCancelable() {
        return this instanceof Cancelable;
    }
    
    /**
     * Checks if the event is canceled.
     *
     * @return true if the event is canceled
     */
    public boolean isCanceled() {
        return canceled && isCancelable();
    }
    
    /**
     * Sets whether the event is canceled.
     *
     * @param canceled true to cancel the event
     * @throws UnsupportedOperationException if the event is not cancelable
     */
    public void setCanceled(boolean canceled) {
        if (!isCancelable()) {
            throw new UnsupportedOperationException("This event cannot be canceled");
        }
        this.canceled = canceled;
    }
}