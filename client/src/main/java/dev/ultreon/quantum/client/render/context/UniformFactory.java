package dev.ultreon.quantum.client.render.context;

import dev.ultreon.quantum.client.render.core.GameUniform;

public interface UniformFactory {
    GameUniform[] createUniforms(GameShader shader);
}
