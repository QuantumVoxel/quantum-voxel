package com.ultreon.quantum.client.gui;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.util.GameRenderable;
import com.ultreon.quantum.entity.player.Player;

public class Hud implements GameRenderable {
    private final QuantumClient client;

    public int leftY;
    public int rightY;
    private int width;
    private int height;


    public Hud(QuantumClient client) {
        this.client = client;
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
    }

    private void renderCrosshair(Renderer renderer) {

    }

    private void renderHotbar(Renderer renderer, Player player) {

    }

    private void renderHealth(Renderer renderer, Player player) {
    }

}
