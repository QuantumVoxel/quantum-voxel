package dev.ultreon.quantum.client.render.context;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Renderable;
import dev.ultreon.quantum.client.render.core.GameUniform;

public class ColorSrc implements ColorSource {
    private final Color color;
    private final String name;
    private final boolean isGlobal;

    public ColorSrc(Color color, String name) {
        this(color, name, false);
    }

    public ColorSrc(Color color, String name, boolean isGlobal) {
        this.color = color;
        this.name = name;
        this.isGlobal = isGlobal;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public boolean isTexture() {
        return false;
    }

    @Override
    public boolean isColor() {
        return true;
    }

    @Override
    public GameUniform[] createUniforms(GameShader shader) {
        return new GameUniform[]{shader.registerUniform("u_" + name + "Color", isGlobal)};
    }

    @Override
    public void assign(ShaderContext context, Renderable renderable, GameUniform uniform) {
        uniform.getShader().set(uniform.getLocation(), color);
    }

    @Override
    public boolean validate(ShaderContext context, Renderable renderable, GameUniform uniform) {
        return true;
    }
}
