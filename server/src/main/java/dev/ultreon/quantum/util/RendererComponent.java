package dev.ultreon.quantum.util;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import org.jetbrains.annotations.Nullable;

public interface RendererComponent extends RenderableProvider {
    /**
     * Obtains the renderables for this component.
     *
     * @deprecated use {@link #getRenderables(Array, Pool, GameObject)} instead
     * @param renderables the output array
     * @param pool the pool to obtain Renderables from
     */
    @Override
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    default void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {

    }

    default void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool, GameObject gameObject) {
        getRenderables(renderables, pool);

        ModelInstance modelInstance = getInstance();
        modelInstance.transform.set(gameObject.transform);
        modelInstance.userData = gameObject;
        modelInstance.getRenderables(renderables, pool);
    }

    ModelInstance getInstance();

    ShaderProvider getShaderProvider();

    void setShaderProvider(@Nullable ShaderProvider provider);
}
