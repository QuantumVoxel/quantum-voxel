package dev.ultreon.quantum.client.render;

import dev.ultreon.quantum.client.render.pass.RenderPass;

public interface VisualObject {
    void render(RenderBufferSource bufferSource, RenderPass pass);
}
