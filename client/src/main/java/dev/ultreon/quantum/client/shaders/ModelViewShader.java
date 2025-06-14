package dev.ultreon.quantum.client.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ModelViewShader extends DefaultShader {
    public final int u_globalSunlight;

    public ModelViewShader(final Renderable renderable) {
        this(renderable, new GeomShaderConfig());
    }

    public ModelViewShader(final Renderable renderable, final Config config) {
        this(renderable, config, "");
    }

    public ModelViewShader(final Renderable renderable, final Config config, final String prefix) {
        this(renderable, config, prefix,
                config.vertexShader != null ? config.vertexShader : getDefaultVertexShader(),
                config.fragmentShader != null ? config.fragmentShader : getDefaultFragmentShader());
    }

    @Override
    public void init() {
        if (!program.isCompiled()) return;

        try {
            super.init();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize model view shader", e);
        }
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        if (!program.isCompiled()) return;
        super.begin(camera, context);
    }

    @Override
    public void render(Renderable renderable) {
        if (!program.isCompiled()) return;
        super.render(renderable);
    }

    @Override
    public void render(Renderable renderable, Attributes combinedAttributes) {
        if (!program.isCompiled()) return;
        super.render(renderable, combinedAttributes);
    }

    @Override
    public boolean canRender(Renderable renderable) {
        return super.canRender(renderable);
    }

    public static String getDefaultGeometryShader() {
        return "void main() {\n" +
               "\n" +
               "}\n";
    }

    public ModelViewShader(final Renderable renderable, final Config config, final String prefix, final String vertexShader,
                           final String fragmentShader) {
        this(renderable, config, new ShaderProgram(prefix + fragmentShader, prefix + vertexShader));
    }

    public ModelViewShader(Renderable renderable, Config config, ShaderProgram shaderProgram) {
        super(renderable, config, shaderProgram);

        this.u_globalSunlight = this.register(WorldShader.Inputs.globalSunlight, WorldShader.Setters.globalSunlight);
    }
}
