package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Clipboard;
import org.jetbrains.annotations.Nullable;


/**
 * Default implementation of IClipboard. Uses Gdx's clipboard implementation.
 */
public class DefaultClipboard implements IClipboard {
    private final Clipboard gdxClipboard;

    /**
     * Constructs a new DefaultClipboard.
     */
    public DefaultClipboard() {
        super();

        // Obtain the Gdx clipboard
        this.gdxClipboard = Gdx.app.getClipboard();
    }

    /**
     * Copies the specified text to the clipboard.
     *
     * @param text The text to be copied.
     * @return True if the text was successfully copied, false otherwise.
     */
    @Override
    public boolean copy(String text) {
        // If the text is null, return false
        if (text == null) return false;

        // Set the clipboard contents to the specified text
        gdxClipboard.setContents(text);

        // Return true to indicate success
        return true;
    }

    /**
     * Retrieves the contents of the clipboard.
     *
     * @return The contents of the clipboard, or null if the clipboard is empty.
     */
    @Override
    public @Nullable String paste() {
        // Return the contents of the clipboard
        return gdxClipboard.getContents();
    }
}
