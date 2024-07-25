package dev.ultreon.quantum.client.shaders;

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.ultreon.libs.commons.v0.vector.Vec2f;
import dev.ultreon.mixinprovider.GeomShaderProgram;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.world.ClientWorld;

public class OutlineShader extends DefaultShader {
    public static final Vector3 CAMERA_UP = new Vector3();
    public final int u_globalSunlight;
    public final int u_atlasSize;
    public final int u_atlasOffset;
    private final int u_lodThreshold;
    private final int u_cameraUp0;
    private String log;


    public OutlineShader(final Renderable renderable) {
        this(renderable, new GeomShaderConfig());
    }

    public OutlineShader(final Renderable renderable, final GeomShaderConfig config) {
        this(renderable, config, "");
    }

    public OutlineShader(final Renderable renderable, final GeomShaderConfig config, final String prefix) {
        this(renderable, config, prefix,
                config.vertexShader != null ? config.vertexShader : getDefaultVertexShader(),
                config.fragmentShader != null ? config.fragmentShader : getDefaultFragmentShader(),
                config.geometryShader != null ? config.geometryShader : getDefaultGeometryShader());
    }

    public static String getDefaultGeometryShader() {
        return """
                void main() {
                
                }
                """;
    }

    public OutlineShader(final Renderable renderable, final Config config, final String prefix, final String vertexShader,
                         final String fragmentShader, String geometryShader) {
        this(renderable, config, new GeomShaderProgram(prefix + vertexShader, prefix + fragmentShader, prefix + geometryShader));
    }

    public OutlineShader(Renderable renderable, Config config, GeomShaderProgram shaderProgram) {
        super(renderable, config, shaderProgram);

        this.u_globalSunlight = this.register(Inputs.globalSunlight, Setters.globalSunlight);
        this.u_atlasSize = this.register(Inputs.atlasSize, Setters.atlasSize);
        this.u_atlasOffset = this.register(Inputs.atlasOffset, Setters.atlasOffset);
        this.u_lodThreshold = this.register(Inputs.lodThreshold, Setters.lodThreshold);
        this.u_cameraUp0 = this.register(Inputs.cameraUp, Setters.cameraUp);
    }
    public static class Inputs extends DefaultShader.Inputs {
        public final static Uniform globalSunlight = new Uniform("u_globalSunlight");
        public final static Uniform atlasSize = new Uniform("u_atlasSize");
        public final static Uniform atlasOffset = new Uniform("u_atlasOffset");
        public final static Uniform lodThreshold = new Uniform("u_lodThreshold");
        public final static Uniform cameraUp = new Uniform("u_cameraUp0");

    }
    public static class Setters extends DefaultShader.Setters {
        public final static Setter globalSunlight = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                ClientWorld world = QuantumClient.get().world;
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

        public final static Setter atlasOffset = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                Vec2f f = ClientWorld.ATLAS_OFFSET.get().f();
                shader.set(inputID, new Vector2(f.x, f.y));
            }
        };

        public final static Setter lodThreshold = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, ClientConfig.lodThreshold);
            }
        };

        public final static Setter cameraUp = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, CAMERA_UP);
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
}
