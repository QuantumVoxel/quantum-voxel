package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import dev.ultreon.quantum.client.Screenshot.ImageSelection;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

public class GameClipboard implements Clipboard {
    private final java.awt.datatransfer.Clipboard toolkitClipboard;
    private final com.badlogic.gdx.utils.Clipboard gdxClipboard;

    public GameClipboard(java.awt.datatransfer.Clipboard toolkitClipboard) {
        super();

        this.toolkitClipboard = toolkitClipboard;
        this.gdxClipboard = Gdx.app.getClipboard();
    }

    @Override
    public boolean copy(BufferedImage image) {
        if (image == null) return false;
        toolkitClipboard.setContents(new ImageSelection(image), null);
        return true;
    }

    @Override
    public boolean copy(String text) {
        if (text == null) return false;
        gdxClipboard.setContents(text);

        return true;
    }

    @Override
    public @Nullable String paste() {
        return gdxClipboard.getContents();
    }
}
