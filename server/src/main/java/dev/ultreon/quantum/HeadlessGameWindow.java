package dev.ultreon.quantum;

public class HeadlessGameWindow extends GameWindow {
    @Override
    public boolean isHovered() {
        return true;
    }

    @Override
    public boolean isMinimized() {
        return false;
    }

    @Override
    public boolean isMaximized() {
        return false;
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    public String getTitle() {
        return "HEADLESS";
    }
}
