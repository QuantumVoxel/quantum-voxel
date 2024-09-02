package dev.ultreon.quantum.client.gui.overlay;

import com.github.tommyettinger.textra.Font;
import dev.ultreon.quantum.client.GameFont;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;

public abstract class Overlay {
    protected static int leftY;
    protected static int rightY;
    protected final QuantumClient client = QuantumClient.get();
    protected final GameFont font = client.font;
    protected int width;
    protected int height;

    protected abstract void render(Renderer renderer, float deltaTime);

    void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
