package dev.ultreon.quantum.client.gui.icon;

import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.util.NamespaceID;

public interface Icon {
    NamespaceID id();

    int width();

    int height();

    int u();

    int v();

    int texWidth();

    int texHeight();

    default void render(Renderer renderer, int x, int y, float deltaTime) {
        renderer.blit(this.id(), x, y, this.width(), this.height(), this.u(), this.v(), this.width(), this.height(), this.texWidth(), this.texHeight());
    }

    default void render(Renderer renderer, float x, float y, int width, int height, float deltaTime) {
        renderer.blit(this.id(), x, y, width, height, this.u(), this.v(), this.width(), this.height(), this.texWidth(), this.texHeight());
    }
}
