package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import dev.ultreon.quantum.client.shaders.ShaderProviders;
import dev.ultreon.quantum.client.shaders.SkyboxShader;
import org.jetbrains.annotations.Nullable;

public class SkyboxShaders extends DefaultShaderProvider implements GameShaders {
    @Nullable
    private final String version;
    private final String name;

    public SkyboxShaders(final DefaultShader.Config config, @Nullable String version, String name) {
        super(config);
        this.version = version;
        this.name = name;
    }

    public SkyboxShaders(final String vertexShader, final String fragmentShader, @Nullable String version, String name) {
        this(new DefaultShader.Config(vertexShader, fragmentShader), version, name);
    }

    public SkyboxShaders(final FileHandle vertexShader, final FileHandle fragmentShader, @Nullable String version, String name) {
        this(vertexShader.readString(), fragmentShader.readString(), version, name);
    }

    public SkyboxShaders(@Nullable String version, String name) {
        this(null, version, name);
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
        SkyboxShader modelViewShader = new SkyboxShader(renderable, this.config, version != null ? "#version " + version + "\n" : "");
        ShaderProviders.checkShaderCompilation(modelViewShader.program, name);
        
        return modelViewShader; 
    }

    public @Nullable String getVersion() {
        return version;
    }

}
