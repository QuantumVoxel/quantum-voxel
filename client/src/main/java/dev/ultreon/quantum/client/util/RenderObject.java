package dev.ultreon.quantum.client.util;

import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.util.GameObject;

public abstract class RenderObject extends GameObject implements Renderable {
    protected RenderPass renderPass;

    public void render(RenderBufferSource bufferSource) {
        if (renderPass != null) {
            bufferSource.getBuffer(renderPass).render(this);
        }

        for (var child : getChildren()) {
            if (child instanceof RenderObject) {
                ((RenderObject) child).render(bufferSource);
            } else if (child instanceof GameObject gameObject && renderPass != null) {
                bufferSource.getBuffer(renderPass).render(gameObject);
            }
        }
    }

    public RenderPass getRenderPass() {
        return renderPass;
    }

    @SuppressWarnings("RedundantThrows")
    public void setRenderPass(RenderPass renderPass) throws ProbibitedOperationException {
        this.renderPass = renderPass;
    }
}
