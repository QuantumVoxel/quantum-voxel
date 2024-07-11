package dev.ultreon.quantum.client;

import org.jetbrains.annotations.Nullable;

public interface IClipboard {
    boolean copy(String text);

    default @Nullable String paste() {
        return null;
    }
}
