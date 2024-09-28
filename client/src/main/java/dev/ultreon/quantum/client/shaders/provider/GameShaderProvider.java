package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import dev.ultreon.quantum.client.render.ShaderContext;

public class GameShaderProvider extends BaseShaderProvider {
    private final DepthShader.Config config;

    public GameShaderProvider(DepthShader.Config config) {
        this.config = config;
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
        GameShaders gameShaders = ShaderContext.get();
        Shader shader = gameShaders.createShader(renderable);
        if (shader == null) throw new IllegalStateException("Shader not found");
        if (!shader.canRender(renderable)) throw new IllegalStateException("Shader cannot render");
        return shader;

//        ShaderMode mode = ShaderContext.getConfig();
//        if (mode == ShaderMode.DEPTH) {
//            return new DepthShader(renderable, this.config);
//        }
//
//        return new DefaultShader(renderable, this.config, DefaultShader.createPrefix(renderable, config), ShaderPrograms.DEFAULT.getVertexShaderSource(), ShaderPrograms.DEFAULT.getFragmentShaderSource());
    }
}
