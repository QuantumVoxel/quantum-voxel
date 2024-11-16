package dev.ultreon.quantum.client;

import io.github.libsdl4j.api.clipboard.SdlClipboard;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of the IClipboard interface using SDL for clipboard operations.
 * <p>
 * This class provides methods to copy text to the clipboard and paste text from the clipboard
 * using SDL 2.
 */
public class LibSdlClipboard implements IClipboard {
    @Override
    public boolean copy(String text) {
        return SdlClipboard.SDL_SetClipboardText(text) == 0;
    }

    @Override
    public @Nullable String paste() {
        if (!SdlClipboard.SDL_HasClipboardText()) {
            return null;
        }
        return SdlClipboard.SDL_GetClipboardText();
    }
}
