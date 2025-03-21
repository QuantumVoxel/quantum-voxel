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
 * @param shaderProvider Provides shaders for the renderables in this model object.
 * @param model Contains the model instance that represents the 3D geometry.
 * @param renderables An array of renderable objects that are part of this model object.
 */
public record ModelObject(GameShaders shaderProvider, ModelInstance model,
                          RenderableArray renderables) implements Disposable {

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


}
