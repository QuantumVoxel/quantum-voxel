package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.shaders.Shaders;
import dev.ultreon.quantum.client.util.RenderObject;

public class CelestialBody extends RenderObject {
    public CelestialBody(Model model, ModelInstance modelInstance) {
        this.renderPass = RenderPass.CELESTIAL_BODIES;
        this.set(StandaloneRenderer.class, new StandaloneRenderer(model, modelInstance, Shaders.MODEL_VIEW.get()));
    }

    public CelestialBody(Model model) {
        this(model, new ModelInstance(model));
    }
}
