package dev.ultreon.quantum.client;

import com.badlogic.gdx.utils.Disposable;

/**
 * A sealed interface for the desktop main class.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public interface DesktopMain extends Disposable {
    /**
     * Resizes the desktop main.
     * 
     * @param width The width.
     * @param height The height.
     */
    void resize(int width, int height);

    /**
     * Renders the desktop main.
     */
    void render();

    /**
     * Pauses the desktop main.
     */
    void pause();

    /**
     * Resumes the desktop main.
     */
    void resume();
}
