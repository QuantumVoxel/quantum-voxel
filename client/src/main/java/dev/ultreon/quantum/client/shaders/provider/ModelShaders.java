package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import dev.ultreon.quantum.client.shaders.GeomShaderConfig;
import dev.ultreon.quantum.client.shaders.ModelViewShader;
import dev.ultreon.quantum.client.shaders.Shaders;

public class ModelShaders extends DefaultShaderProvider implements GameShaders {
    protected final DefaultShader.Config config;

    public ModelShaders(final DefaultShader.Config config) {
        super(config);
        this.config = config;
    }

    public ModelShaders(final String vertexShader, final String fragmentShader) {
        this(new DefaultShader.Config(vertexShader, fragmentShader));
    }

    public ModelShaders(final FileHandle vertexShader, final FileHandle fragmentShader) {
        this(vertexShader.readString(), fragmentShader.readString());
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
