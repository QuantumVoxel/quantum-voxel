package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import dev.ultreon.quantum.client.render.shader.GameShaders;
import dev.ultreon.quantum.client.render.shader.Shaders;
import dev.ultreon.quantum.client.shaders.GeomShaderConfig;
import dev.ultreon.quantum.client.shaders.ModelViewShader;

public class ModelViewShaderProvider extends DefaultShaderProvider implements GameShaders {
    private final GeomShaderConfig config;

    public ModelViewShaderProvider(final GeomShaderConfig config) {
        super(config);
        this.config = config;
    }

    public ModelViewShaderProvider(final String vertexShader, final String fragmentShader, String geometryShader) {
        this(new GeomShaderConfig(vertexShader, fragmentShader, geometryShader));
    }

    public ModelViewShaderProvider(final FileHandle vertexShader, final FileHandle fragmentShader, FileHandle geometryShader) {
        this(vertexShader.readString(), fragmentShader.readString(), geometryShader.readString());
    }

    public ModelViewShaderProvider() {
        this(new GeomShaderConfig());
    }

    @Override
    public Shader createShader(Renderable renderable) {
        ModelViewShader modelViewShader = new ModelViewShader(renderable, this.config);
        Shaders.checkShaderCompilation(modelViewShader.program, "ModelViewShader");
        
        return modelViewShader; 
    }

}
