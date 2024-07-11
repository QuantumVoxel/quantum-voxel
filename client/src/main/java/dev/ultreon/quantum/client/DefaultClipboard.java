package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Clipboard;
import org.jetbrains.annotations.Nullable;

public class DefaultClipboard implements IClipboard {
    private final Clipboard gdxClipboard;

    public DefaultClipboard() {
        super();

        this.gdxClipboard = Gdx.app.getClipboard();
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
