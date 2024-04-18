package com.ultreon.quantum.client.gui.overlay;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.gui.Renderer;
import com.ultreon.quantum.entity.Player;

public class HealthOverlay extends Overlay {
    @Override
    protected void render(Renderer renderer, float deltaTime) {
        Player player = this.client.player;
        if (player == null) return;

        int x = (int) ((float) this.client.getScaledWidth() / 2) - 88;

        var iconsTex = this.client.getTextureManager().getTexture(QuantumClient.id("textures/gui/icons.png"));
        for (int emptyHeartX = 0; emptyHeartX < 10; emptyHeartX++)
            renderer.blit(iconsTex, x + emptyHeartX * 8, leftY - 9, 9, 9, 16, 0);

        int heartX;
        for (heartX = 0; heartX < Math.floor(player.getHealth() / 2); heartX++)
            renderer.blit(iconsTex, x + heartX * 8, leftY - 9, 9, 9, 25, 0);

        if ((int) player.getHealth() % 2 == 1)
            renderer.blit(iconsTex, x + heartX * 8, leftY - 9, 9, 9, 34, 0);

        leftY -= 13;
    }
}
