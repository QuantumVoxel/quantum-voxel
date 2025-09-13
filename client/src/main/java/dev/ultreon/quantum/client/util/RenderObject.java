package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.collision.Ray;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderType;
import dev.ultreon.quantum.util.GameObject;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public abstract class RenderObject extends GameObject implements Renderable {
    protected RenderType renderType;

    public void render(RenderBufferSource bufferSource) {
        if (renderType != null) {
            bufferSource.getBuffer(renderType).render(this);
        }

        for (var child : getChildren()) {
            if (child instanceof RenderObject) {
                ((RenderObject) child).render(bufferSource);
            } else if (child instanceof GameObject && renderType != null) {
                GameObject gameObject = (GameObject) child;
                bufferSource.getBuffer(renderType).render(gameObject);
            }
        }
    }

    public RenderType getRenderPass() {
        return renderType;
    }

    @SuppressWarnings("RedundantThrows")
    public void setRenderPass(RenderType renderType) throws ProbibitedOperationException {
        this.renderType = renderType;
    }

    public List<GameObject> hit(Ray ray) {
        return new ArrayList<>();
    }
}
