package dev.ultreon.quantum.client.render.core;

import dev.ultreon.quantum.client.render.context.GameShader;
import dev.ultreon.quantum.client.shaders.WorldShader;

public class GameUniform {
    private final GameShader shader;
    private final int location;
    private final String name;
    private final boolean isGlobal;

    public GameUniform(GameShader shader, int location, String name, boolean isGlobal) {
        this.shader = shader;
        this.location = location;
        this.name = name;
        this.isGlobal = isGlobal;
    }

    public String getName() {
        return name;
    }

    public GameShader getShader() {
        return shader;
    }

    public int getLocation() {
        return location;
    }

    public boolean isGlobal() {
        return isGlobal;
    }
}
