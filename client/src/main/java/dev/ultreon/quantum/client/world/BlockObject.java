package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import dev.ultreon.quantum.client.shaders.ShaderProviders;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.InstanceRenderer;

public class BlockObject extends GameObject {
    private final ModelInstance modelInstance;

    public BlockObject(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;

        this.set(InstanceRenderer.class, new InstanceRenderer(modelInstance, ShaderProviders.MODEL_VIEW.get()));
    }
}
