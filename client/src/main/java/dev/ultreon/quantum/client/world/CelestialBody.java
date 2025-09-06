package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;
import dev.ultreon.quantum.client.render.RenderType;
import dev.ultreon.quantum.client.render.context.RenderMaterial;
import dev.ultreon.quantum.client.util.RenderObject;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class CelestialBody extends RenderObject {
    public static final ObjectSet<NamespaceID> REGISTRY = new ObjectSet<>();

    static {
        REGISTRY.add(NamespaceID.of("sun"));
        REGISTRY.add(NamespaceID.of("moon"));
    }

    private final Model model;
    private final ModelInstance modelInstance;
    public final RenderMaterial material;

    public CelestialBody(Model model, RenderMaterial material) {
        this.material = material;
        this.renderType = RenderType.CELESTIAL_BODIES;
        this.model = model;
        this.modelInstance = new ModelInstance(model);
    }

    @Override
    public void getRenderables(@NotNull Array<Renderable> renderables, @NotNull Pool<Renderable> pool) {
        update(Gdx.graphics.getDeltaTime());
        modelInstance.transform.set(combined);
        modelInstance.getRenderables(renderables, pool);
    }

    public Model getModel() {
        return model;
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }
}
