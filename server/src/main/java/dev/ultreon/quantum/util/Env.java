package dev.ultreon.quantum.util;

import org.jetbrains.annotations.NotNull;

public enum Env {
    CLIENT,
    SERVER;

    public @NotNull Env opposite() {
        return this == CLIENT ? SERVER : CLIENT;
    }
}
