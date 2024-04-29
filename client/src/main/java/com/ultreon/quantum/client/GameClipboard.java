package com.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.ultreon.quantum.client.Screenshot.ImageSelection;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;

public class GameClipboard implements IClipboard {
    private final Clipboard toolkitClipboard;
    private final com.badlogic.gdx.utils.Clipboard gdxClipboard;

    public GameClipboard(Clipboard toolkitClipboard) {
        super();

        this.toolkitClipboard = toolkitClipboard;
        this.gdxClipboard = Gdx.app.getClipboard();
    }

    @Override
    public void copy(BufferedImage image) {
        toolkitClipboard.setContents(new ImageSelection(image), null);
    }

    @Override
    public void copy(String text) {
        gdxClipboard.setContents(text);
    }

    @Override
    public @Nullable String paste() {
        return gdxClipboard.getContents();
    }
}
