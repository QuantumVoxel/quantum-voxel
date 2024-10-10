package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;

public interface LodRenderableProvider extends RenderableProvider {
    @Override
    @Deprecated
    default void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        QuantumClient.LOGGER.warn("Call to deprecated method 'getRenderables' for " + getClass().getName() + "!");
        getLodRenderables(renderables, pool, CommonConstants.DEFAULT_LOD_LEVEL);
    }

    void getLodRenderables(Array<Renderable> renderables, Pool<Renderable> pool, int lodLevel);
}
