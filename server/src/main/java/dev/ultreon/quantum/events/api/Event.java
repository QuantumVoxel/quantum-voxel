package dev.ultreon.quantum.events.api;

import java.util.ArrayList;
import java.util.List;

/**
 * An event that can be subscribed to and unsubscribed from.
 *
 * <h2>Example</h2>
 * <pre>
 * public class Events {
 *     public static final Event&lt;TheEvent&gt; THE_EVENT = Event.create();
 *
 *     &#64;FunctionalInterface
 *     public interface TheEvent {
 *         void onTheEvent();
 *     }
 * }
 * </pre>
 *
 * @param <T>
 */
public final class Event<T> {
    private final Factory<T> factory;
    private final List<T> subscribers = new ArrayList<>();

    public Event(Factory<T> factory) {
        this.factory = factory;
    }

    @Deprecated
    public void listen(T t) {
        this.subscribers.add(t);
    }

    public void subscribe(T t) {
        this.subscribers.add(t);
    }

    public void unsubscribe(T t) {
        this.subscribers.remove(t);
    }

    public T factory() {
        return this.factory.create(this.subscribers);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Event<T> create(Factory<T> factory, T... typeGetter) {
        if (typeGetter.length != 0) throw new IllegalStateException("The array shouldn't contain anything!");
        return of((Class<T>) typeGetter.getClass().getComponentType(), factory);
    }

    public static <T> Event<T> of(Class<T> clazz, Factory<T> factory) {
        return new Event<>(factory);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Event<T> withResult(Factory<T> factory, T... typeGetter) {
        if (typeGetter.length != 0) throw new IllegalStateException("The array shouldn't contain anything!");
        return withResult((Class<T>) typeGetter.getClass().getComponentType(), factory);
    }

    public static <T> Event<T> withResult(Class<T> clazz, Factory<T> factory) {
        return new Event<>(factory);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Event<T> withValue(Factory<T> factory, T... typeGetter) {
        if (typeGetter.length != 0) throw new IllegalStateException("The array shouldn't contain anything!");
        return withValue((Class<T>) typeGetter.getClass().getComponentType(), factory);
    }

    public static <T> Event<T> withValue(Class<T> clazz, Factory<T> factory) {
        return new Event<>(factory);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Event<T> cancelable(Factory<T> factory, T... typeGetter) {
        if (typeGetter.length != 0) throw new IllegalStateException("The array shouldn't contain anything!");
        return cancelable((Class<T>) typeGetter.getClass().getComponentType(), factory);
    }

    public static <T> Event<T> cancelable(Class<T> clazz, Factory<T> factory) {
        return new Event<>(factory);
    }

    public interface Factory<T> {
        T create(List<T> listeners);
    }
}
