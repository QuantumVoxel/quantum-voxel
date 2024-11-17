package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.ultreon.quantum.component.GameComponent;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.RendererComponent;
import org.jetbrains.annotations.Nullable;

public class StandaloneRenderer extends GameComponent implements RendererComponent {
    private final Model model;
    private final ModelInstance modelInstance;
    private ShaderProvider shaderProvider;

    public StandaloneRenderer(Model model, ShaderProvider shaderProvider) {
        this.model = model;
        this.modelInstance = new ModelInstance(model);
        this.shaderProvider = shaderProvider;
    }

    public StandaloneRenderer(Model model, ModelInstance modelInstance, ShaderProvider shaderProvider) {
        this.model = model;
        this.modelInstance = modelInstance;
        this.shaderProvider = shaderProvider;
    }

    public Model getModel() {
        return model;
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    @Override
    public void dispose() {
        super.dispose();
        model.dispose();
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool, GameObject gameObject) {
        modelInstance.transform.set(gameObject.transform);
        modelInstance.calculateTransforms();

        modelInstance.getRenderables(renderables, pool);
    }

    @Override
    public ModelInstance getInstance() {
        return modelInstance;
    }

    @Override
    public ShaderProvider getShaderProvider() {
        return shaderProvider;
    }

    @Override
    public void setShaderProvider(@Nullable ShaderProvider provider) {

    }
}
