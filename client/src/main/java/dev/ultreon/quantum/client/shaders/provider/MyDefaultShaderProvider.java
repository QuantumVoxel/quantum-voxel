package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;

public class MyDefaultShaderProvider extends DefaultShaderProvider implements GameShaders {
    public MyDefaultShaderProvider(DefaultShader.Config config) {
        super(config);
    }

    public MyDefaultShaderProvider(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public MyDefaultShaderProvider(FileHandle vertexShader, FileHandle fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public MyDefaultShaderProvider() {
    }

    @Override
    public Shader createShader(Renderable renderable) {
        return super.createShader(renderable);
    }

    @Override
    public Shader getShader(Renderable renderable) {
        try {
            return super.getShader(renderable);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get shader from default shader provider", e);
        }
    }
}
