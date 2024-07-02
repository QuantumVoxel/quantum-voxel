package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.client.gui.Matrices;
import dev.ultreon.quantum.client.input.GameCamera;
import dev.ultreon.quantum.client.render.TextureSamplers;
import org.checkerframework.common.reflection.qual.NewInstance;

public class CollectNode extends RenderPipeline.RenderNode {
    @NewInstance
    @Override
    public Array<Renderable> render(Matrices matrices, TextureSamplers samplers, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input, float deltaTime) {
        return input;
    }
}
