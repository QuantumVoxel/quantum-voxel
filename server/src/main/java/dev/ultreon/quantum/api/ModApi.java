package dev.ultreon.quantum.api;

import dev.ultreon.quantum.api.event.GlobalEventHandler;

public final class ModApi {
    private static ModApi instance;

    private final GlobalEventHandler globalEventHandler = new GlobalEventHandler();

    private ModApi() {

    }

    public static ModApi init() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }

        return instance = new ModApi();
    }

    public static GlobalEventHandler getGlobalEventHandler() {
        return instance.globalEventHandler;
    }
}
