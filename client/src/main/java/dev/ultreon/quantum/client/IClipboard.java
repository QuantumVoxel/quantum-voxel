package dev.ultreon.quantum.client;

import org.jetbrains.annotations.Nullable;

/**
 * Interface for a clipboard.
 * <p>
 * This interface provides methods for copying and pasting text to/from the clipboard.
 */
public interface IClipboard {
    /**
     * Copies the given {@code text} to the clipboard.
     *
     * @param text the text to copy
     * @return {@code true} if the text was successfully copied, {@code false} otherwise
     */
    boolean copy(String text);

    /**
     * Retrieves the text currently on the clipboard.
     *
     * @return the text on the clipboard, or {@code null} if there is no text on the clipboard
     */
    @Nullable
    default String paste() {
        return null;
    }
}
