package dev.ultreon.quantum.client.render.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.client.shaders.GeomShaderConfig;
import dev.ultreon.quantum.client.shaders.OutlineShader;

public class OutlineShaderProvider extends DefaultShaderProvider implements GameShaders {
    public OutlineShaderProvider(ResourceFileHandle resourceFileHandle, ResourceFileHandle resourceFileHandle1) {
        
    }

    @Override
    public Shader createShader(Renderable renderable) {
        OutlineShader outlineShader = new OutlineShader(renderable, new GeomShaderConfig(
                QuantumClient.resource(QuantumClient.id("shaders/outline.vert")).readString(),
                QuantumClient.resource(QuantumClient.id("shaders/outline.frag")).readString(),
                QuantumClient.resource(QuantumClient.id("shaders/outline.geom")).readString()
        ));
        if (!outlineShader.program.isCompiled()) {
            Gdx.app.error("OutlineShader", outlineShader.program.getLog());
        }
        return outlineShader;
    }
}
