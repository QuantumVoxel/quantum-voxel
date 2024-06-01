package dev.ultreon.quantum.client.render.shader;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;

public interface GameShaders {
    Shader createShader(Renderable renderable);
}
