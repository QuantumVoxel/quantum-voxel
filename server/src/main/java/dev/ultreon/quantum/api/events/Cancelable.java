package dev.ultreon.quantum.api.events;

public interface Cancelable {
    boolean isCanceled();

    void setCanceled(boolean canceled);
}
