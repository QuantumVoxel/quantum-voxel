package com.ultreon.quantum.client;

import java.awt.image.BufferedImage;

public class NullClipboard implements IClipboard {
    @Override
    public void copy(BufferedImage image) {

    }

    @Override
    public void copy(String text) {

    }
}
