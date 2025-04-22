package dev.ultreon.quantum.api.event;

import java.util.Objects;

public final class PriortizedEventListener<T extends Event> implements EventListener<T> {
    private final int priority;

    public PriortizedEventListener(int priority) {
        this.priority = priority;
    }

    @Override
    public void call(T event) {

    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PriortizedEventListener) obj;
        return this.priority == that.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority);
    }

    @Override
    public String toString() {
        return "PriortizedEventListener[" +
               "priority=" + priority + ']';
    }

}
