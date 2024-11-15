package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.input.GameCamera;
import org.checkerframework.common.reflection.qual.NewInstance;

public class CollectNode extends RenderPipeline.RenderNode {
    @NewInstance
    @Override
    public void render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, float deltaTime) {

    }
}
