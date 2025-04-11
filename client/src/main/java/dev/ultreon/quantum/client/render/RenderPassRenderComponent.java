package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import dev.ultreon.quantum.component.GameComponent;
import dev.ultreon.quantum.util.RendererComponent;

public class RenderPassRenderComponent extends GameComponent implements RendererComponent {
    private final ModelInstance modelInstance;
    private final RenderPass renderPass;

    public RenderPassRenderComponent(ModelInstance modelInstance, RenderPass renderPass) {
        this.modelInstance = modelInstance;
        this.renderPass = renderPass;
    }

    @Override
    public ModelInstance getInstance() {
        return modelInstance;
    }

    public RenderPass getRenderPass() {
        return renderPass;
    }

    public void render(RenderBufferSource bufferSource) {
        bufferSource.getBuffer(renderPass).render(this);
    }
}
