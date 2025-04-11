package dev.ultreon.quantum.client.util;

import dev.ultreon.quantum.client.gui.Renderer;

public interface GuiRenderable {
    void render(Renderer renderer, float deltaTime);
}
