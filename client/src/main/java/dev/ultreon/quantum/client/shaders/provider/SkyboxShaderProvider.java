package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import dev.ultreon.quantum.client.render.shader.OpenShaderProvider;
import dev.ultreon.quantum.client.render.shader.Shaders;
import dev.ultreon.quantum.client.shaders.SkyboxShader;

public class SkyboxShaderProvider extends DefaultShaderProvider implements OpenShaderProvider {
    public SkyboxShaderProvider(final DefaultShader.Config config) {
        super(config);
    }

    public SkyboxShaderProvider(final String vertexShader, final String fragmentShader) {
        this(new DefaultShader.Config(vertexShader, fragmentShader));
    }

    public SkyboxShaderProvider(final FileHandle vertexShader, final FileHandle fragmentShader) {
        this(vertexShader.readString(), fragmentShader.readString());
    }

    public SkyboxShaderProvider() {
        this(null);
    }

    @Override
    public Shader createShader(Renderable renderable) {
        SkyboxShader modelViewShader = new SkyboxShader(renderable, this.config);
        Shaders.checkShaderCompilation(modelViewShader.program, "SkyboxShader");
        
        return modelViewShader; 
    }

}
