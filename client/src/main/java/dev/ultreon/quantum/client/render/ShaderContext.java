package dev.ultreon.quantum.client.render;

import dev.ultreon.quantum.client.render.shader.GameShaders;

public class ShaderContext {
    private static GameShaders mode;

    public static void set(GameShaders mode) {
        ShaderContext.mode = mode;
    }

    public static GameShaders get() {
        return ShaderContext.mode;
    }

    public enum ShaderMode {
        DEPTH,
        DIFFUSE
    }
}
