package dev.ultreon.quantum.client.gui.widget;

import dev.ultreon.quantum.client.gui.Renderer;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

/**
 * Static widget, only for rendering.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public interface StaticWidget {
    /**
     * Renders this widget.
     *
     * @param renderer  The renderer
     * @param deltaTime The delta time of the rendering
     */
    void render(@NotNull Renderer renderer, @IntRange(from = 0) float deltaTime);
}
