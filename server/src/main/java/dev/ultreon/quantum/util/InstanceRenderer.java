package dev.ultreon.quantum.util;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.ultreon.quantum.component.GameComponent;
import org.jetbrains.annotations.Nullable;

public class InstanceRenderer extends GameComponent implements RendererComponent {
    private final ModelInstance instance;
    private ShaderProvider shaderProvider;

    public InstanceRenderer(ModelInstance instance, ShaderProvider shaderProvider) {
        this.instance = instance;
        this.shaderProvider = shaderProvider;
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool, GameObject gameObject) {
        instance.transform.set(gameObject.transform);
        instance.getRenderables(renderables, pool);
    }

    public ModelInstance getInstance() {
        return instance;
    }

    public ShaderProvider getShaderProvider() {
        return shaderProvider;
    }

    @Override
    public void setShaderProvider(@Nullable ShaderProvider provider) {
        shaderProvider = provider;
    }
}
