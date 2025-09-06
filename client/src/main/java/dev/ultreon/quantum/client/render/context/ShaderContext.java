package dev.ultreon.quantum.client.render.context;

import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.TextureBinder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.ultreon.quantum.client.shaders.BaseGameShader;

public class ShaderContext {
    private final GameShader gameShader;
    private final ShaderProgram shaderProgram;
    private final TextureBinder textureBinder = new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN);

    public ShaderContext(BaseGameShader baseGameShader) {
        this.gameShader = baseGameShader;
        this.shaderProgram = baseGameShader.getShaderProgram();
    }

    public GameShader getGameShader() {
        return gameShader;
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    public TextureBinder getTextureBinder() {
        return textureBinder;
    }
}
