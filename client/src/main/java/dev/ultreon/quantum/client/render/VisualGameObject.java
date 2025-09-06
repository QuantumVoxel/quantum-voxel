package dev.ultreon.quantum.client.render;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.render.context.RenderMaterial;
import dev.ultreon.quantum.client.render.pass.RenderPass;
import dev.ultreon.quantum.client.util.ProbibitedOperationException;
import dev.ultreon.quantum.util.GameObject;

public abstract class VisualGameObject extends GameObject implements VisualObject {
    protected RenderMaterial renderMaterial;
    private boolean loggedWarning;

    @Override
    public void render(RenderBufferSource bufferSource, RenderPass pass) {
        RenderType type = null;
        if (renderMaterial != null) {
            type = pass.renderTypeFor(renderMaterial);
            if (type == null) {
                if (!loggedWarning) {
                    loggedWarning = true;
                    CommonConstants.LOGGER.warn("No render type found for game object: " + this.getName() + " (material: " + renderMaterial.getName() + ")");
                }
                return;
            }
            bufferSource.getBuffer(type).render(this);
        }

        for (var child : getChildren()) {
            if (child instanceof VisualGameObject) {
                ((VisualGameObject) child).render(bufferSource, pass);
            } else if (child instanceof GameObject && renderMaterial != null) {
                GameObject gameObject = (GameObject) child;
                bufferSource.getBuffer(type).render(gameObject);
            }
        }
    }

    public RenderMaterial getRenderMaterial() {
        return renderMaterial;
    }

    @SuppressWarnings("RedundantThrows")
    public void setRenderMaterial(RenderMaterial renderType) throws ProbibitedOperationException {
        this.renderMaterial = renderType;
    }
}
