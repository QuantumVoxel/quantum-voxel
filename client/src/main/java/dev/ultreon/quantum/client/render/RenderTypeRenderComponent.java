package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import dev.ultreon.quantum.component.GameComponent;
import dev.ultreon.quantum.util.RendererComponent;

public class RenderTypeRenderComponent extends GameComponent implements RendererComponent {
    private final ModelInstance modelInstance;
    private final RenderType renderType;

    public RenderTypeRenderComponent(ModelInstance modelInstance, RenderType renderType) {
        this.modelInstance = modelInstance;
        this.renderType = renderType;
    }

    @Override
    public ModelInstance getInstance() {
        return modelInstance;
    }

    public RenderType getRenderPass() {
        return renderType;
    }

    public void render(RenderBufferSource bufferSource) {
        bufferSource.getBuffer(renderType).render(this);
    }
}
