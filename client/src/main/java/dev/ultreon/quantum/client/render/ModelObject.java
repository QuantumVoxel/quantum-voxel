package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.render.shader.GameShaders;
import dev.ultreon.quantum.client.util.RenderableArray;

import java.util.Objects;

public final class ModelObject implements Disposable {
    private final GameShaders shaderProvider;
    private final ModelInstance model;
    private final RenderableArray renderables;

    public ModelObject(GameShaders shaderProvider, ModelInstance model, RenderableArray renderables) {
        this.shaderProvider = shaderProvider;
        this.model = model;
        this.renderables = renderables;
    }

    public void dispose() {
        for (Renderable renderable : renderables) {
            renderable.meshPart.mesh = null;
            renderable.userData = null;
        }
        renderables.clear();
    }

    @Override
    public String toString() {
        return "ModelObject[" +
               "shaderProvider=" + shaderProvider + ", " +
               "renderables=" + renderables + ']';
    }

    public GameShaders shaderProvider() {
        return shaderProvider;
    }

    public ModelInstance model() {
        return model;
    }

    public RenderableArray renderables() {
        return renderables;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ModelObject) obj;
        return Objects.equals(this.shaderProvider, that.shaderProvider) &&
               Objects.equals(this.model, that.model) &&
               Objects.equals(this.renderables, that.renderables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shaderProvider, model, renderables);
    }


}
