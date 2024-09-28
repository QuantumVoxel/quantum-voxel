package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.shaders.GeomShaderConfig;
import dev.ultreon.quantum.client.shaders.OutlineShader;

public class OutlineShaderProvider extends DefaultShaderProvider implements GameShaders {
    public OutlineShaderProvider(FileHandle resourceFileHandle, FileHandle resourceFileHandle1) {
        
    }

    @Override
    public Shader getShader(Renderable renderable) {
        try {
            return super.getShader(renderable);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get shader from default shader provider", e);
        }
    }

    @Override
    public Shader createShader(Renderable renderable) {
        OutlineShader outlineShader = new OutlineShader(renderable, new GeomShaderConfig(
                QuantumClient.shader(QuantumClient.id("outline.vert")).readString(),
                QuantumClient.shader(QuantumClient.id("outline.frag")).readString(),
                QuantumClient.shader(QuantumClient.id("outline.geom")).readString()
        ));
        if (!outlineShader.program.isCompiled())
            Gdx.app.error("OutlineShader", outlineShader.program.getLog());
        return outlineShader;
    }
}
