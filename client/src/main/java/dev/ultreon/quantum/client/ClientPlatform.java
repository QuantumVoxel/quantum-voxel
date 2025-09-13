package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.util.IVec2;

public abstract class ClientPlatform extends GamePlatform {
    protected ClientPlatform() {
        super();

        if (isDevEnvironment()) {
            this.setShowingImGui(true);
        }
    }

    public void getWindowOffset(IVec2 windowOffset) {
        windowOffset.set(0, 0);
    }

    public static ClientPlatform get() {
        return (ClientPlatform) GamePlatform.get();
    }

    public void setShowingImGui(boolean value) {
        // Implemented in subclasses
    }

    public boolean isShowingImGui() {
        return false;
    }

    public void preInitImGui() {
        // Implemented in subclasses
    }

    public void setTextCursorPos(int x, int y) {
        // Implemented in subclasses
    }

    public void onEnterTextInput() {
        Gdx.input.setOnscreenKeyboardVisible(true);
    }

    public void onExitTextInput() {
        Gdx.input.setOnscreenKeyboardVisible(false);
    }

    public void setupImGui() {
        // Implemented in subclasses
    }

    public void renderImGui() {
        // Implemented in subclasses
    }

    public void onFirstRender() {
        // Implemented in subclasses
    }

    public boolean isImGuiSupported() {
        return false;
    }
}
