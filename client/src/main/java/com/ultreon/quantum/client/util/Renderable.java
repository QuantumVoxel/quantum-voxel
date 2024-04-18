package com.ultreon.quantum.client.util;

import com.ultreon.quantum.client.gui.Renderer;

public interface Renderable {
    void render(Renderer renderer, int mouseX, int mouseY, float deltaTime);
}
