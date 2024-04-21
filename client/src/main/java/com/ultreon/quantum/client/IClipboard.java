package com.ultreon.quantum.client;

import java.awt.image.BufferedImage;

public interface IClipboard {
    void copy(BufferedImage image);

    void copy(String text);
}
