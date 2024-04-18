package com.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.ultreon.quantum.CommonConstants;
import com.ultreon.quantum.client.ClientRegistries;
import com.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

public class RenderLayer {
    public static final RenderLayer DEFAULT = RenderLayer.register("default", new RenderLayer());
    public static final RenderLayer WATER = RenderLayer.register("water", new RenderLayer());

    private static RenderLayer register(String name, RenderLayer renderType) {
        ClientRegistries.RENDER_LAYER.register(new Identifier(CommonConstants.NAMESPACE, name), renderType);
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
