package dev.ultreon.quantum.client.gui.overlay;

import com.badlogic.gdx.math.MathUtils;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.util.NamespaceID;

public class HealthOverlay extends Overlay {
    @Override
    protected void render(Renderer renderer, float deltaTime) {
        Player player = this.client.player;
        if (player == null) return;
        if (player.isInvincible()) return;

        int x = (int) ((float) this.client.getScaledWidth() / 2) - 20;

        int frames = 10;

        int frame = MathUtils.round(frames * (player.getMaxHealth() - player.getHealth()) / player.getMaxHealth());
        renderer.blit(this.client.getTextureManager().getTexture(new NamespaceID("textures/gui/hotbar.png")), x, height - 20 - 35, 40, 40, 180, 112, 40, 40, 400, 256);
        renderer.blit(this.client.getTextureManager().getTexture(new NamespaceID("textures/gui/hotbar.png")), x, height - 20 - 36, 40, 34, 180, 112 + 40, 40, 34, 400, 256);
        renderer.blit(this.client.getTextureManager().getTexture(new NamespaceID("textures/gui/hotbar.png")), x + 3, height - 20 - 32, 34, 28, 365 - frame * 35, 59, 34, 28, 400, 256);

//        int delU = 2;
//        if (player.regenFlashTimer % 2 == 0) delU = 29;
//
//        var iconsTex = this.client.getTextureManager().getTexture(QuantumClient.id("textures/gui/icons.png"));
//        for (int emptyHeartX = 0; emptyHeartX < 10; emptyHeartX++)
//            renderer.blit(iconsTex, x + emptyHeartX * 8, leftY - 9, 9, 9, 34 + delU, 0);
//
//        int heartX;
//        for (heartX = 0; heartX < Math.floor(player.getHealth() / 2); heartX++)
//            renderer.blit(iconsTex, x + heartX * 8, leftY - 9, 9, 9, 16 + delU, 0);
//
//        if ((int) player.getHealth() % 2 == 1)
//            renderer.blit(iconsTex, x + heartX * 8, leftY - 9, 9, 9, 25 + delU, 0);
//
//        leftY -= 13;
    }
}
