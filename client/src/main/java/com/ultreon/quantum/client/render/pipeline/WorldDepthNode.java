package com.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.common.base.Supplier;
import com.ultreon.quantum.client.init.Shaders;
import com.ultreon.quantum.client.input.GameCamera;
import org.checkerframework.common.reflection.qual.NewInstance;

public class WorldDepthNode extends WorldRenderNode {
    private final Supplier<DepthShaderProvider> shaderProvider = Shaders.DEPTH;

    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
        this.render(modelBatch, this.shaderProvider.get(), input);
        textures.put("depth", this.getFrameBuffer().getColorBufferTexture());
        return input;
    }
}
