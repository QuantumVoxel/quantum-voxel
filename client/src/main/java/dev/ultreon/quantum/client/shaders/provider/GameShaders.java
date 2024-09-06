package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;

public interface GameShaders {
    Shader createShader(Renderable renderable);
}
