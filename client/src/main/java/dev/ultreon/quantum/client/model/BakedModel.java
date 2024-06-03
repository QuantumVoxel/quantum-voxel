package dev.ultreon.quantum.client.model;

import com.badlogic.gdx.graphics.g3d.Model;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.util.Identifier;

public abstract class BakedModel {
    private final Model model;
    private final Identifier resourceId;

    public BakedModel(Identifier resourceId, Model model) {
        this.resourceId = resourceId;
        this.model = model;

        ModelManager.INSTANCE.add(resourceId, model);
    }

    public Model getModel() {
        return model;
    }

    public final Identifier resourceId() {
        return resourceId;
    }
}
