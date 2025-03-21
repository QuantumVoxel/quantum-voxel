package dev.ultreon.quantum.client;

/**
 * A null implementation of the IClipboard interface.
 * Used when the platform does not support clipboard operations.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class NullClipboard implements IClipboard {
    /**
     * Copies the given text to the clipboard.
     * 
     * @param text The text to copy.
     * @return False, as the clipboard is not supported.
     */
    @Override
    public boolean copy(String text) {
        return false;
    }
}
