package dev.ultreon.quantum.client.gui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.client.gui.overlay.Overlay;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.util.Vec;

public class BuriedBlockOverlay extends Overlay {
    private final Vec tmp = new Vec();

    @Override
    protected void render(Renderer renderer, float deltaTime) {
        LocalPlayer player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) return;

        BlockState blockState = world.get(player.getPosition(deltaTime, tmp).add(0, player.getEyeHeight(), 0));
        BlockModel blockModel = client.getBlockModel(blockState);
        TextureRegion buriedTexture = blockModel.getBuriedTexture();
        if (buriedTexture == null) return;

        renderer.blit(buriedTexture, 0, 0, width, height);
    }
}
