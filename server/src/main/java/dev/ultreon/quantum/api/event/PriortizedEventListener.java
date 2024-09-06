package dev.ultreon.quantum.api.event;

public record PriortizedEventListener<T extends Event>(int priority) implements EventListener<T> {
    @Override
    public void call(T event) {

    }
}
