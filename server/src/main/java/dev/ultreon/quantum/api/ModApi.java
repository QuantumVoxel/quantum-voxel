package dev.ultreon.quantum.api;

import dev.ultreon.quantum.api.event.EventSystem;
import org.jetbrains.annotations.Nullable;

public final class ModApi {
    private static final Object lock = new Object();
    private static @Nullable ModApi instance;

    private static final EventSystem eventSystem = new EventSystem();

    private ModApi() {

    }

    public static ModApi init() {
        synchronized (lock) {
            if (instance != null) {
                throw new IllegalStateException("Already initialized");
            }

            return instance = new ModApi();
        }
    }

    public static EventSystem getEventSystem() {
        return eventSystem;
    }
}
