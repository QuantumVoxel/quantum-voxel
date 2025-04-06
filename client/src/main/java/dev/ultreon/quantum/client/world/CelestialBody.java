package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import dev.ultreon.quantum.client.shaders.Shaders;
import dev.ultreon.quantum.util.GameObject;

public class CelestialBody extends GameObject {
    public CelestialBody(Model model, ModelInstance modelInstance) {
        this.set(StandaloneRenderer.class, new StandaloneRenderer(model, modelInstance, Shaders.MODEL_VIEW.get()));
    }

    public CelestialBody(Model model) {
        this(model, new ModelInstance(model));
    }
}
