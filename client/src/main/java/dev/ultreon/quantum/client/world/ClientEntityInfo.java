package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.InstanceRenderer;

public class ClientEntityInfo extends GameObject {
    private final ModelInstance modelInstance;

    public ClientEntityInfo(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;

        this.set(InstanceRenderer.class, new InstanceRenderer(modelInstance, null));
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }
}
