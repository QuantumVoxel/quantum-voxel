package dev.ultreon.quantum.client;

import io.github.libsdl4j.api.clipboard.SdlClipboard;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of the IClipboard interface using SDL for clipboard operations.
 * <p>
 * This class provides methods to copy text to the clipboard and paste text from the clipboard
 * using SDL 2.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @deprecated Use {@link DefaultClipboard} instead, this class will be removed in future version.
 *             Deprecation due to compatibility issues with macOS.
 */
@Deprecated(forRemoval = true)
public class LibSdlClipboard implements IClipboard {
    /**
     * Copies the given text to the clipboard.
     * 
     * @param text The text to copy.
     * @return True if the text was copied successfully, false otherwise.
     */
    @Override
    public boolean copy(String text) {
        return SdlClipboard.SDL_SetClipboardText(text) == 0;
    }

    /**
     * Pastes the text from the clipboard.
     * 
     * @return The text from the clipboard, or null if there is no text in the clipboard.
     */
    @Override
    public @Nullable String paste() {
        if (!SdlClipboard.SDL_HasClipboardText()) {
            return null;
        }
        return SdlClipboard.SDL_GetClipboardText();
    }
}
