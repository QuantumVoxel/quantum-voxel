package dev.ultreon.quantum.client.gui;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.widget.StaticWidget;
import dev.ultreon.quantum.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Hud implements StaticWidget {
    private final QuantumClient client;

    public int leftY;
    public int rightY;
    private int width;
    private int height;


    public Hud(QuantumClient client) {
        this.client = client;
    }

    @Override
    public void render(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
    }

    private void renderCrosshair(Renderer renderer) {

    }

    private void renderHotbar(Renderer renderer, Player player) {

    }

    private void renderHealth(Renderer renderer, Player player) {
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        QuantumClient.get().notifications.addOnce(UUID.fromString("d04e6cc8-df0c-4520-935a-8262cac97c2c"), Notification.builder("HUD", "MouseOver check not implemented for HUD").build());
        return false;
    }

    public boolean touchDown(int mouseX, int mouseY, int pointer, int button) {
        return false;
    }

    public boolean touchUp(int mouseX, int mouseY, int pointer, int button) {
        return false;
    }
}
