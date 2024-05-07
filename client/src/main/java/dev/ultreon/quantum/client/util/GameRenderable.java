package dev.ultreon.quantum.client.util;

import dev.ultreon.quantum.client.gui.Renderer;

public interface GameRenderable {
    void render(Renderer renderer, float deltaTime);
}
