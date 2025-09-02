package dev.ultreon.quantum.api.event;

import dev.ultreon.baseskript.BaseSkript;
import dev.ultreon.baseskript.event.EventBus;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.api.events.Cancelable;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * A global event handler that can be used to register event listeners.
 * <p>
 * This event handler is used to register listeners for various events that occur in the Quantum API.
 * It is a central place to handle events instead of having to register listeners individually for each event.
 * <p>
 * The handler is implemented as a singleton, which means that there is only one instance of the event handler
 * throughout the application.
 * <p>
 * Listeners are registered using the {@link EventSystem#addListener(Class, EventListener)} method.
 * The method takes two parameters: the class of the event to listen for and the actual listener object.
 * The listener object must implement the {@link EventListener} interface and must be able to handle events of the specified type.
 * Or they can use it as a lambda expression or method reference.
 * <p>
 * Event listeners are sorted automatically when they are added to the handler.
 * The order is determined by the priority value of the listener,
 * which is defined by the {@link EventListener#priority()} method.
 * <p>
 * The event handler can be accessed using the {@link ModApi#getEventSystem()} method.
 */
public final class EventSystem {
    private final Map<Class<? extends Event>, ArrayList<EventListener<?>>> listeners = new HashMap<>();

    @ApiStatus.Internal
    public EventSystem() {
        // This is here for the annotation, nothing else.
    }

    /**
     * Adds a listener to the global event handler.
     * <p>
     * The listener is added to the list of listeners for the specified event type.
     * The listeners are sorted automatically when they are added, based on the priority value of the listener
     * (defined by the {@link EventListener#priority()} method).
     * <p>
     * If the event type has not been registered before, a new list of listeners is created.
     * The list of listeners is sorted after adding the new listener.
     *
     * @param type the class of the event to listen for
     * @param listener the listener to add
     * @param <T> the type of the event
     */
    public <T extends Event> void addListener(Class<T> type, EventListener<T> listener) {
        synchronized (this) {
            if (!this.listeners.containsKey(type))
                // We don't need to do anything since there's no listener list registered for this event.
                return;

            ArrayList<EventListener<?>> eventListeners = this.listeners.get(type);

            // Add and re-sort the event listener list.
            eventListeners.add(listener);
            eventListeners.sort(EventListener::compareTo);
        }
    }

    /**
     * Removes a listener from the global event handler.
     * <p>
     * The listener is removed from the list of listeners for the specified event type.
     * If the listener is not found in the list, nothing happens.
     *
     * @param type the class of the event to listen for
     * @param listener the listener to remove
     * @param <T> the type of the event
     */
    public <T extends Event> void removeListener(Class<T> type, EventListener<T> listener) {
        synchronized (this) {
            if (!this.listeners.containsKey(type))
                // We don't need to do anything since there's no listener list registered for this event.
                return;

            // Remove the listener.
            ArrayList<EventListener<?>> eventListeners = this.listeners.get(type);
            eventListeners.remove(listener);

            if (eventListeners.isEmpty())
                // No event listeners left, remove the event type.
                this.listeners.remove(type);
        }
    }

    /**
     * Calls all registered listeners for the provided event.
     * <p>
     * The event is passed to each listener, and if any of the listeners cancel the event (by setting {@code true} in
     * the {@link Cancelable#setCanceled(boolean)} method), the calling process is interrupted and the method
     * returns {@code true}. If all listeners successfully process the event without cancellation, the method
     * returns {@code false}.
     *
     * @param event the event to be called
     * @param <T>   the type of the event
     * @return {@code true} if the event was canceled, {@code false} otherwise
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> boolean post(T event) {
        // A stack of classes to check for listeners.
        Stack<Class<?>> superClasses = new Stack<>();

        BaseSkript.getEventBus().publish(event);

        // Start with the class of the event itself.
        superClasses.push(event.getClass());

        // Loop through each class in the stack.
        while (!superClasses.isEmpty()) {
            // Get the current class.
            Class<?> current = superClasses.pop();

            // Check if the current class is not Object or Event, and if so, add its superclass to the stack.
            if (current != null && current != Object.class && current != Event.class) {
                Class<?> superclass = current.getSuperclass();
                if (superclass != null && superclass != Object.class) superClasses.push(superclass);

                // Loop through each interface of the current class.
                // If an interface has the annotation AnEvent, add it to the stack.
                for (Class<?> superInterface : current.getInterfaces()) {
                    superClasses.push(superInterface);
                    break;
                }
            }

            // Loop through each listener for the current class.
            Map<Class<? extends Event>, ArrayList<EventListener<?>>> listeners = this.listeners;
            synchronized (this) {
                if (!listeners.containsKey(current)) continue;
                for (EventListener<?> listener : listeners.get((Class<? extends Event>) current)) {
                    try {
                        // Cast the listener to the specific type of the event and call it with the event.
                        ((EventListener<T>) listener).call(event);

                        // If the event was canceled, return true.
                        if (event instanceof Cancelable && ((Cancelable) event).isCanceled()) {
                            Cancelable cancelable = (Cancelable) event;
                            return cancelable.isCanceled();
                        }
                    } catch (Throwable t) {
                        // If an exception occurs, log it and continue to the next listener.
                        CommonConstants.LOGGER.error("An exception occurred while handling an event:", t);
                    }
                }
            }
        }

        // Return whether the event was canceled.
        return event instanceof Cancelable && ((Cancelable) event).isCanceled();
    }

    /**
     * Clears all listeners for the given event type.
     *
     * @param type the type of the event to clear the listeners for
     */
    public void clear(Class<? extends Event> type) {
        this.listeners.remove(type);
    }

    /**
     * Clears all listeners from the global event handler.
     */
    public void clear() {
        this.listeners.clear();
    }

    public static <T extends Event> void postDefault(T event) {
        EventSystem eventSystem = ModApi.getEventSystem();
        eventSystem.post(event);
    }

    public static <T extends Event & Cancelable> boolean postCancelable(T event) {
        EventSystem eventSystem = ModApi.getEventSystem();
        return eventSystem.post(event);
    }

    public static <T extends Event> void addListenerDefault(Class<T> type, EventListener<T> listener) {
        EventSystem eventSystem = ModApi.getEventSystem();
        eventSystem.addListener(type, listener);
    }

    public static <T extends Event> void removeListenerDefault(Class<T> type, EventListener<T> listener) {
        EventSystem eventSystem = ModApi.getEventSystem();
        eventSystem.removeListener(type, listener);
    }
}
