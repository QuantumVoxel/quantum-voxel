package dev.ultreon.quantum.client;

public class NullClipboard implements IClipboard {
    @Override
    public boolean copy(String text) {
        return false;
    }
}
