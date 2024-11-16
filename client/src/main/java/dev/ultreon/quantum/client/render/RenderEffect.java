package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.ClientRegistries;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.ApiStatus;

/**
 * The RenderEffect class represents different visual effects that can be applied during rendering.
 * It is used to register and manage different rendering effects.
 */
public class RenderEffect {
    public static final RenderEffect DEFAULT = RenderEffect.register("default", new RenderEffect());
    public static final RenderEffect WATER = RenderEffect.register("water", new RenderEffect());

    private static RenderEffect register(String name, RenderEffect renderType) {
        ClientRegistries.RENDER_EFFECT.register(new NamespaceID(CommonConstants.NAMESPACE, name), renderType);
        return renderType;
    }

    public void nopInit() {
        // Load class
    }

    @ApiStatus.Experimental
    public Shader getShader(Renderable renderable) {
        return new DefaultShader(renderable);
    }
}
