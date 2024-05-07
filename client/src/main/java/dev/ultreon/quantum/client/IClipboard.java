package dev.ultreon.quantum.client;

import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

public interface IClipboard {
    void copy(BufferedImage image);

    void copy(String text);

    default @Nullable String paste() {
        return null;
    }
}
