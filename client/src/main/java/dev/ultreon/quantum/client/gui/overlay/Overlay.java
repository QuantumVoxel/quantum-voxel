package dev.ultreon.quantum.client.gui.overlay;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.font.Font;
import dev.ultreon.quantum.client.gui.Renderer;

public abstract class Overlay {
    protected static int leftY;
    protected static int rightY;
    protected final QuantumClient client = QuantumClient.get();
    protected final Font font = client.font;
    protected int width;
    protected int height;

    protected abstract void render(Renderer renderer, float deltaTime);

    void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
