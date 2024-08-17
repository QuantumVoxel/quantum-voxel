package dev.ultreon.quantum.api.event;

public interface EventListener<T extends Event> extends Comparable<EventListener<?>> {
    default int priority() {
        return 1000;
    }

    void call(T event);

    default int compareTo(EventListener<?> eventListener) {
        return Integer.compare(this.priority(), eventListener.priority());
    }
}
