package com.ultreon.quantum.client.util;

import com.ultreon.quantum.client.gui.Renderer;

public interface GameRenderable {
    void render(Renderer renderer, float deltaTime);
}
