package dev.ultreon.quantum.client.gui.overlay;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;

/**
 * Crosshair overlay class for the Quantum Voxel.
 *
 * @author XyperCode
 * @since 0.1.0
 */
public class CrosshairOverlay extends Overlay {
    /**
     * Draws the crosshair.
     * By default, the crosshair is drawn at the center of the screen.
     *
     * @param renderer the renderer to draw with
     * @param deltaTime the time passed since the last frame
     */
    @Override
    protected void render(Renderer renderer, float deltaTime) {
        // Enable invert
        renderer.invertOn();

        // Draw crosshair
        renderer.blit(QuantumClient.id("textures/gui/icons.png"), width / 2f - 4.5f, height / 2f - 4.5f, 9, 9, 0, 0, 9, 9);

        // Disable invert
        renderer.invertOff();
    }
}
