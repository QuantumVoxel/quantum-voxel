package dev.ultreon.quantum.client.util;

import dev.ultreon.quantum.client.gui.Renderer;

public interface Renderable {
    void render(Renderer renderer, float deltaTime);
}
