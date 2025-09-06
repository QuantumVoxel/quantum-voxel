package dev.ultreon.quantum.client.render.context;

import com.badlogic.gdx.graphics.g3d.Renderable;
import dev.ultreon.quantum.client.render.core.GameUniform;

public interface UniformSetter {
    void assign(ShaderContext context, Renderable renderable, GameUniform uniform);

    boolean validate(ShaderContext context, Renderable renderable, GameUniform uniform);
}
