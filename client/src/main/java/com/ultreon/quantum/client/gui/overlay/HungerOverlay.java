package com.ultreon.quantum.client.gui.overlay;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.gui.Renderer;
import com.ultreon.quantum.entity.Player;

public class HungerOverlay extends Overlay {
    @Override
    protected void render(Renderer renderer, float deltaTime) {
        Player player = this.client.player;
        if (player == null) return;
        if (player.isInvincible()) return;

        int x = (int) ((float) this.client.getScaledWidth() / 2) + 80;

        var iconsTex = this.client.getTextureManager().getTexture(QuantumClient.id("textures/gui/icons.png"));
        for (int emptyHeartX = 0; emptyHeartX < 10; emptyHeartX++)
            renderer.blit(iconsTex, x - emptyHeartX * 8, rightY - 9, 9, 9, 16, 10);

        int foodX;
        for (foodX = 0; foodX < player.getFoodStatus().getFoodLevel() / 2; foodX++)
            renderer.blit(iconsTex, x - foodX * 8, rightY - 9, 9, 9, 25, 10);

        if ((int) player.getHealth() % 2 == 1)
            renderer.blit(iconsTex, x - foodX * 8, rightY - 9, 9, 9, 34, 10);

        rightY -= 13;
    }
}
