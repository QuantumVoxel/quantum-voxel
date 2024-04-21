package com.ultreon.quantum.client;

import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;

public class GameClipbard implements IClipboard {
    private final Clipboard toolkitClipboard;

    public GameClipbard(Clipboard toolkitClipboard) {
        super();

        this.toolkitClipboard = toolkitClipboard;
    }

    @Override
    public void copy(BufferedImage image) {
        toolkitClipboard.setContents(new Screenshot.ImageSelection(image), null);
    }

    @Override
    public void copy(String text) {
//        toolkitClipboard.setContents(new TextSelection(text), null);
    }
}
