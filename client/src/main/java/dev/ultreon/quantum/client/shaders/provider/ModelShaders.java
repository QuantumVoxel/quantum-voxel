package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import dev.ultreon.quantum.client.shaders.GeomShaderConfig;
import dev.ultreon.quantum.client.shaders.ModelViewShader;
import dev.ultreon.quantum.client.shaders.Shaders;

public class ModelShaders extends DefaultShaderProvider implements GameShaders {
    protected final GeomShaderConfig config;

    public ModelShaders(final GeomShaderConfig config) {
        super(config);
        this.config = config;
    }

    public ModelShaders(final String vertexShader, final String fragmentShader, String geometryShader) {
        this(new GeomShaderConfig(vertexShader, fragmentShader, geometryShader));
    }

    public ModelShaders(final FileHandle vertexShader, final FileHandle fragmentShader, FileHandle geometryShader) {
        this(vertexShader.readString(), fragmentShader.readString(), "");
    }

    public ModelShaders() {
        this(new GeomShaderConfig());
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
        ModelViewShader modelViewShader = new ModelViewShader(renderable, this.config);
        Shaders.checkShaderCompilation(modelViewShader.program, "ModelViewShader");
        
        return modelViewShader; 
    }

}
