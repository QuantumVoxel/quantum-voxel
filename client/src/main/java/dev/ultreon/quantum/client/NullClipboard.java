package dev.ultreon.quantum.client;

import java.awt.image.BufferedImage;

public class NullClipboard implements Clipboard {
    @Override
    public boolean copy(BufferedImage image) {
        return false;
    }

    @Override
    public boolean copy(String text) {
        return false;
    }
}
