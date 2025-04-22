package dev.ultreon.quantum.android;

import com.badlogic.gdx.Gdx;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.GameWindow;

public class AndroidWindow extends GameWindow {
    private String title;

    @Override
    public long getHandle() {
        return 0;
    }

    @Override
    public void close() {
        Gdx.app.exit();
    }

    @Override
    public void requestAttention() {
        GamePlatform gamePlatform = AndroidPlatform.get();
        AndroidPlatform androidPlatform = (AndroidPlatform) gamePlatform;

        androidPlatform.sendNotification("attention", "Quantum", "Quantum requires attention");
    }

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
        return true;
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    public void setResizable(boolean resizable) {
        // When true allow rotation
        // When false turn on rotation lock

        if (resizable) {
            GamePlatform gamePlatform = AndroidPlatform.get();
            AndroidPlatform androidPlatform = (AndroidPlatform) gamePlatform;
            androidPlatform.setRotationLock(true);
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
}
