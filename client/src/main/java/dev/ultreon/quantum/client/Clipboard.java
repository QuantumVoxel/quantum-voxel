package dev.ultreon.quantum.client;

import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

public interface Clipboard {
    boolean copy(BufferedImage image);

    boolean copy(String text);

    default @Nullable String paste() {
        return null;
    }
}
