package dev.ultreon.quantum.client.render.shader;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;

public class OutlineShaderProvider extends DefaultShaderProvider implements GameShaders {
    public OutlineShaderProvider(ResourceFileHandle resourceFileHandle, ResourceFileHandle resourceFileHandle1) {
        
    }

    @Override
    public Shader createShader(Renderable renderable) {
        return new OutlineShader(renderable);
    }
}
