package dev.ultreon.quantum.client.shaders;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A shader implementation specifically designed for rendering a skybox.
 * The SkyboxShader extends the DefaultShader class and provides the necessary
 * uniforms and setters to handle skybox-specific color interpolation and rendering.
 */
public class SkyboxShader extends DefaultShader {
    public final int u_topColor;
    public final int u_midColor;
    public final int u_bottomColor;

    public final int u_negZColor;
    public final int u_posZColor;

    public SkyboxShader(final Renderable renderable) {
        this(renderable, new Config());
    }

    public SkyboxShader(final Renderable renderable, final Config config) {
        this(renderable, config, "");
    }

    public SkyboxShader(final Renderable renderable, final Config config, final String prefix) {
        this(renderable, config, prefix, config.vertexShader != null ? config.vertexShader : getDefaultVertexShader(),
                config.fragmentShader != null ? config.fragmentShader : getDefaultFragmentShader());
    }

    public SkyboxShader(final Renderable renderable, final Config config, final String prefix, final String vertexShader,
                        final String fragmentShader) {
        this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
    }

    public SkyboxShader(Renderable renderable, Config config, ShaderProgram shaderProgram) {
        super(renderable, config, shaderProgram);

        this.u_topColor = this.register(Inputs.topColor, Setters.topColor);
        this.u_midColor = this.register(Inputs.midColor, Setters.midColor);
        this.u_bottomColor = this.register(Inputs.bottomColor, Setters.bottomColor);

        this.u_negZColor = this.register(Inputs.negZColor, Setters.negZColor);
        this.u_posZColor = this.register(Inputs.posZColor, Setters.posZColor);
    }

    public static class Inputs extends DefaultShader.Inputs {
        public final static Uniform topColor = new Uniform("u_topColor");
        public final static Uniform midColor = new Uniform("u_midColor");
        public final static Uniform bottomColor = new Uniform("u_bottomColor");

        public final static Uniform negZColor = new Uniform("u_negZColor");
        public final static Uniform posZColor = new Uniform("u_posZColor");
    }


    public static class Setters extends DefaultShader.Setters {
        public final static Setter topColor = create((w) -> w.getSkybox().topColor);
        public final static Setter midColor = create((w) -> w.getSkybox().midColor);
        public final static Setter bottomColor = create((w) -> w.getSkybox().bottomColor);

        public final static Setter negZColor = create((w) -> w.getSkybox().negZColor);
        public final static Setter posZColor = create((w) -> w.getSkybox().posZColor);

        public static Setter create(Function<TerrainRenderer, Color> getter) {
            return new LocalSetter() {
                @Override
                public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                    if (renderable == null) {
                        shader.set(inputID, Color.WHITE);
                        return;
                    }
                    @Nullable TerrainRenderer world = QuantumClient.get().worldRenderer;
                    if (world == null) {
                        shader.set(inputID, Color.WHITE);
                        return;
                    }
                    shader.set(inputID, getter.apply(QuantumClient.get().worldRenderer));
                }
            };
        }
    }

    @Override
    public void init() {
        try {
            super.init();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize skybox shader", e);
        }
    }
}
