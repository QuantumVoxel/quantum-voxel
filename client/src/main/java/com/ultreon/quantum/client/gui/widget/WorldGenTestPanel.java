package com.ultreon.quantum.client.gui.widget;

import com.ultreon.quantum.client.gui.Renderer;
import com.ultreon.quantum.util.Color;
import com.ultreon.quantum.world.TerrainNoise;

import java.util.Random;

public class WorldGenTestPanel extends Panel {
    private TerrainNoise terrainNoise;
    private int terrainX = 0;

    public WorldGenTestPanel() {
        super(0, 0, 0, 255);

        this.terrainNoise = new TerrainNoise(new Random(0).nextLong());
    }

    @Override
    protected void renderBackground(Renderer renderer, float deltaTime) {
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        TerrainNoise noise = this.terrainNoise;
        if (noise == null) return;

        for (int i = 0; i < size.width; i++) {
            int height = (int) noise.evaluateNoise(i + terrainX);
            renderer.line(pos.x + i, size.height, pos.x + i, size.height - height, Color.rgb(height <= 64 ? 0x0055aa : 0x55aa55));
        }
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        if (rotation > 0) {
            terrainX += (int) (10 * rotation);
        } else {
            terrainX += (int) (10 * rotation);
        }

        return super.mouseWheel(mouseX, mouseY, rotation);
    }

    @Override
    public boolean charType(char character) {
        if (character == ' ') {
            this.terrainNoise = new TerrainNoise(new Random(0).nextLong());
        }

        return super.charType(character);
    }

    public void dispose() {
        this.terrainNoise = null;
    }

    public TerrainNoise getTerrainNoise() {
        return terrainNoise;
    }

    public static WorldGenTestPanel create() {
        return new WorldGenTestPanel();
    }
}
