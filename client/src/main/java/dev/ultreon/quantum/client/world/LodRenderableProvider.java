package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;

/**
 * The `LodRenderableProvider` interface extends the `RenderableProvider` interface
 * and provides a method to obtain renderables with a specified level of detail (LOD).
 * <p>
 * This interface introduces a new method `getLodRenderables` which allows specifying
 * the level of detail for the renderables being obtained. The default implementation
 * of the deprecated `getRenderables` method logs a warning and delegates to the
 * `getLodRenderables` method with a default LOD level.
 */
public interface LodRenderableProvider extends RenderableProvider {
    /**
     * Deprecated method to get renderables.
     * This method logs a warning and delegates to the `getLodRenderables` method with a default LOD level.
     *
     * @param renderables the array to store the renderables
     * @param pool the pool to get new renderables from
     */
    @Override
    @Deprecated
    default void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        QuantumClient.LOGGER.warn("Call to deprecated method 'getRenderables' for {}!", getClass().getName());
        getLodRenderables(renderables, pool, CommonConstants.DEFAULT_LOD_LEVEL);
    }

    /**
     * Provides renderables with a specific level of detail (LOD).
     *
     * @param renderables the array to store the renderables
     * @param pool the pool to get new renderables from
     * @param lodLevel the level of detail for the renderables
     */
    void getLodRenderables(Array<Renderable> renderables, Pool<Renderable> pool, int lodLevel);
}
