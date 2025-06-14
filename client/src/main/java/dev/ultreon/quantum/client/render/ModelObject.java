package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.shaders.provider.GameShaders;
import dev.ultreon.quantum.client.util.RenderableArray;

import java.util.Objects;

/**
 * Represents a model object in a graphics application. This class contains the
 * necessary elements to render a 3D model, including shader information, the model
 * instance, and an array of renderables.
 * <p>
 * The class implements the {@link Disposable} interface to allow for proper resource
 * management, specifically to clear the resources associated with the contained renderables.
 * </p> 
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public final class ModelObject implements Disposable {
    private final GameShaders shaderProvider;
    private final ModelInstance model;
    private final RenderableArray renderables;

    /**
     * @param shaderProvider Provides shaders for the renderables in this model object.
     * @param model Contains the model instance that represents the 3D geometry.
     * @param renderables An array of renderable objects that are part of this model object.
     */
    public ModelObject(GameShaders shaderProvider, ModelInstance model,
                       RenderableArray renderables) {
        this.shaderProvider = shaderProvider;
        this.model = model;
        this.renderables = renderables;
    }

    /**
     * Disposes of the resources held by the contained renderables.
     * <p>
     * This method iterates over each {@code Renderable} in the {@code renderables} array,
     * setting their {@code meshPart.mesh} and {@code userData} to {@code null}, effectively
     * releasing these resources.
     * </p>
     *
     * <p>
     * Finally, it clears the {@code renderables} array to ensure that all references are removed.
     * </p>
     */
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
