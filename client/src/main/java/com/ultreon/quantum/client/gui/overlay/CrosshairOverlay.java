package com.ultreon.quantum.client.gui.overlay;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.gui.Renderer;

public class CrosshairOverlay extends Overlay {
    @Override
    protected void render(Renderer renderer, float deltaTime) {
        renderer.invertOn();

        float x = this.width / 2f;
        float y = this.height / 2f;
        renderer.blit(QuantumClient.id("textures/gui/icons.png"), width / 2f, height / 2f, 9, 9, 0, 0, 9, 9);

        renderer.invertOff();
    }
}
