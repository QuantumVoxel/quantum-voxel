package com.ultreon.quantum.client.gui.overlay;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.gui.Renderer;

public class CrosshairOverlay extends Overlay {
    @Override
    protected void render(Renderer renderer, float deltaTime) {
        renderer.invertOn();

        float x = this.width / 2f;
        float y = this.height / 2f;
        renderer.blit(QuantumClient.id("textures/gui/crosshair.png"), x - 4.5f, y - 4.5f, 9, 9);

        renderer.invertOff();
    }
}
