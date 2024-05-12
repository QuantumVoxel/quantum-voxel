package dev.ultreon.quantum;

import com.badlogic.gdx.Gdx;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;

/**
 * Represents the game window using LWJGL3.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public abstract class GameWindow {
    private final long handle;
    private boolean resizable;

    /**
     * Initializes the game window by getting the window handle from LibGDX.
     */
    public GameWindow() {
        this.handle = 0;
    }

    /**
     * Get the handle of the game window.
     *
     * @return The handle of the window.
     */
    public long getHandle() {
        return handle;
    }

    /**
     * Closes the game window.
     */
    public void close() {
        GamePlatform.get().close();
    }

    /**
     * Sets the visibility of the game window.
     *
     * @param visible True to make the window visible, false to hide it.
     */
    public void setVisible(boolean visible) {
        GamePlatform.get().setVisible(visible);
    }

    /**
     * Requests attention to the game window by flashing it.
     */
    public void requestAttention() {
        GamePlatform.get().requestAttention();
    }

    @ApiStatus.Experimental
    public abstract boolean isHovered();

    public void setTitle(String title) {
        Gdx.graphics.setTitle(title);
    }

    public abstract boolean isMinimized();

    public abstract boolean isMaximized();

    public abstract boolean isFocused();

    public void minimize() {

    }

    public void maximize() {

    }

    public void restore() {

    }

    public void focus() {

    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public boolean isResizable() {
        return resizable;
    }
}
