package com.ultreon.quantum.client.render;

import com.ultreon.quantum.client.render.shader.OpenShaderProvider;

public class ShaderContext {
    private static OpenShaderProvider mode;

    public static void set(OpenShaderProvider mode) {
        ShaderContext.mode = mode;
    }

    public static OpenShaderProvider get() {
        return ShaderContext.mode;
    }

    public enum ShaderMode {
        DEPTH,
        DIFFUSE
    }
}
