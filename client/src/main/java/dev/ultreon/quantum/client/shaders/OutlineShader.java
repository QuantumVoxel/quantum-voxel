package dev.ultreon.quantum.client.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.util.Vec2f;
import org.jetbrains.annotations.Nullable;

public class OutlineShader extends DefaultShader {
    public static final Vector3 CAMERA_UP = new Vector3();
    public final int u_globalSunlight;
    public final int u_atlasSize;
    public final int u_lodThreshold;
    public final int u_cameraUp0;
    public final int u_viewportSize;
    public final int u_lineWidth;

    public OutlineShader(final Renderable renderable) {
        this(renderable, new GeomShaderConfig());
    }

    public OutlineShader(final Renderable renderable, final GeomShaderConfig config) {
        this(renderable, config, "");
    }

    public OutlineShader(final Renderable renderable, final GeomShaderConfig config, final String prefix) {
        this(renderable, config, prefix,
                config.vertexShader != null ? config.vertexShader : getDefaultVertexShader(),
                config.fragmentShader != null ? config.fragmentShader : getDefaultFragmentShader()
        );
    }

    public static String getDefaultGeometryShader() {
        return "void main() {\n" +
               "\n" +
               "}\n";
    }

    public OutlineShader(final Renderable renderable, final Config config, final String prefix, final String vertexShader,
                         final String fragmentShader) {
        this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
    }

    public OutlineShader(Renderable renderable, Config config, ShaderProgram shaderProgram) {
        super(renderable, config, shaderProgram);

        this.u_globalSunlight = this.register(Inputs.globalSunlight, Setters.globalSunlight);
        this.u_atlasSize = this.register(Inputs.atlasSize, Setters.atlasSize);
        this.u_lodThreshold = this.register(Inputs.lodThreshold, Setters.lodThreshold);
        this.u_cameraUp0 = this.register(Inputs.cameraUp, Setters.cameraUp);
        this.u_viewportSize = this.register(Inputs.viewportSize, Setters.viewportSize);
        this.u_lineWidth = this.register(Inputs.lineWidth, Setters.lineWidth);
    }
    public static class Inputs extends DefaultShader.Inputs {
        public final static Uniform globalSunlight = new Uniform("u_globalSunlight");
        public final static Uniform atlasSize = new Uniform("u_atlasSize");
        public final static Uniform atlasOffset = new Uniform("u_atlasOffset");
        public final static Uniform lodThreshold = new Uniform("u_lodThreshold");
        public final static Uniform cameraUp = new Uniform("u_cameraUp0");
        public final static Uniform viewportSize = new Uniform("u_viewportSize");
        public final static Uniform lineWidth = new Uniform("u_lineWidth");
    }
    public static class Setters extends DefaultShader.Setters {
        public final static Setter globalSunlight = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                @Nullable ClientWorldAccess world = QuantumClient.get().world;
                if (world != null) {
                    shader.set(inputID, world.getGlobalSunlight());
                } else {
                    shader.set(inputID, 0);
                }
            }
        };

        public final static Setter atlasSize = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                Vec2f f = ClientWorld.ATLAS_SIZE.get().f();
                shader.set(inputID, new Vector2(f.x, f.y));
            }
        };

        public final static Setter lodThreshold = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, ClientConfiguration.lodThreshold.getValue());
            }
        };

        public final static Setter cameraUp = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, CAMERA_UP);
            }
        };

        public final static Setter viewportSize = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, new Vector2(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight()));
            }
        };

        public final static Setter lineWidth = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, 6.0F);
            }
        };
    }

    @Override
    public void render(Renderable renderable) {
        try {
            super.render(renderable);
        } catch (GdxRuntimeException e) {
            QuantumClient.LOGGER.error("Failed to render renderable with mesh part ID: {}", renderable.meshPart.id, e);
        }
    }

    @Override
    public void init() {
        try {
            super.init();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize outline shader", e);
        }
    }
}
