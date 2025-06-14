package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.Graphics;
import dev.ultreon.quantum.client.gui.Renderer;

@Deprecated
public interface Drawable {
    /**
     * Rendering method, should not be called if you don't know what you are doing.
     *
     * @param renderer  renderer to draw/render with.
     * @param mouseX
     * @param mouseY
     * @param deltaTime value of {@link Graphics#getDeltaTime()}
     */
    void render(Renderer renderer, int mouseX, int mouseY, float deltaTime);
}
