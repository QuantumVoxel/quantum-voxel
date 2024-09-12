package dev.ultreon.quantum.client;

import io.github.libsdl4j.api.clipboard.SdlClipboard;
import org.jetbrains.annotations.Nullable;

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
