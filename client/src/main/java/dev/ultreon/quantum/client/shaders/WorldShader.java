package dev.ultreon.quantum.client.shaders;

import com.badlogic.gdx.graphics.Color;
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
import dev.ultreon.quantum.client.render.GraphicsSetting;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.util.Vec2f;
import dev.ultreon.quantum.world.Chunk;
import org.jetbrains.annotations.Nullable;

/**
 * The WorldShader class extends the DefaultShader to provide advanced rendering capabilities tailored
 * for world rendering within the game's context. It supports multiple configurations including geometry
 * shaders, level of detail (LOD) control, and various shader inputs for attributes such as fog color,
 * global sunlight, and atlas properties.
 * <p>
 * This shader is suitable for rendering chunks of the game world, supporting both general-purpose rendering
 * and LOD-specific rendering. It overrides and extends fundamental methods of DefaultShader to include custom
 * rendering logic, such as handling chunk-specific LOD levels and additional shader inputs.
 * <p>
 * The class supports a variety of configurations through different constructors, allowing the use of default
 * or customized shader programs. Additionally, it ensures compatibility with the rendering pipeline by
 * checking conditions like LOD compatibility for a given renderable object.
 * <p>
 * Internal utility methods such as `version()` determine the shading language version based on the platform in use,
 * while `getDefaultGeometryShader()` provides default implementations for geometry shaders. Shader uniform variables
 * and their setters are registered and managed to facilitate runtime updates of shader properties, such as fog color
 * and atlas-related data.
 * <p>
 * Subclasses may provide further specifics for rendering their own objects by augmenting this class's methods or
 * adding additional uniforms and logic.
 */
public class WorldShader extends DefaultShader {
    public static final Vector3 CAMERA_UP = new Vector3();
    public static final float TEXTURE_SIZE = 16f;
    public final int u_globalSunlight;
    public final int u_atlasSize;
    public final int u_atlasOffset;
    public final int u_lod;
    public final int u_lodThreshold;
    public final int u_cameraUp0;
    public final int u_fogColor;
    public final int u_chunkPosition;
    public final int u_shadowPixelSize;
    private int lod = -1;
    protected String log;

    /**
     * Constructs a new WorldShader instance with the specified renderable,
     * using a default geometry shader configuration.
     *
     * @param renderable The Renderable instance to be rendered.
     */
    public WorldShader(final Renderable renderable) {
        this(renderable, new GeomShaderConfig());
    }

    /**
     * Constructs a new WorldShader instance with the specified renderable and shader configuration.
     *
     * @param renderable The Renderable instance to be rendered.
     * @param config The configuration for the geometry shader, containing shader sources and settings.
     */
    public WorldShader(final Renderable renderable, final GeomShaderConfig config) {
        this(renderable, config, "");
    }

    /**
     * Constructs a new WorldShader instance with a specified renderable, shader configuration,
     * and a prefix for resolving shader source paths.
     *
     * @param renderable The Renderable instance to be rendered.
     * @param config The configuration for the geometry shader, containing shader sources and settings.
     * @param prefix A string prefix used to resolve shader source paths.
     */
    public WorldShader(final Renderable renderable, final Config config, final String prefix) {
        this(renderable, config, prefix,
                config.vertexShader != null ? config.vertexShader : getDefaultVertexShader(),
                config.fragmentShader != null ? config.fragmentShader : getDefaultFragmentShader());
    }

    /**
     * Constructs a WorldShader instance with a specified renderable, shader configuration, and level of detail (LOD).
     *
     * @param renderable The Renderable instance to be rendered.
     * @param config The configuration for the geometry shader, containing shader sources and settings.
     * @param lod The level of detail used for rendering, influencing the #define LOD_LEVEL in the shader.
     */
    public WorldShader(Renderable renderable, Config config, int lod) {
        this(renderable, config, String.format("#define LOD_LEVEL %s\n", lod));
        this.lod = lod;
    }

    /**
     * Determines if this shader can render the provided renderable object.
     * The method first delegates to the superclass's canRender method and ensures
     * the shader is compatible with the renderable based on its level of detail (LOD).
     *
     * @param renderable The renderable object to check for compatibility with this shader.
     *                   It is typically an instance of {@link Renderable}.
     * @return True if the shader can render the provided renderable, false otherwise.
     */
    @Override
    public boolean canRender(Renderable renderable) {
        boolean b = super.canRender(renderable);
        if (!b) return false;
        if (renderable.userData instanceof ClientChunk) {
            ClientChunk clientChunk = (ClientChunk) renderable.userData;
            return clientChunk.lod == lod;
        }
        return lod == -1;
    }

    /**
     * Provides the default geometry shader source code as a string.
     *
     * @return A string containing the source code for the default geometry shader.
     */
    public static String getDefaultGeometryShader() {
        return "void main() {\n" +
               "\n" +
               "}\n";
    }

    /**
     * Constructs a WorldShader instance with the specified parameters, initializing
     * a geometry shader program using the provided vertex, fragment, and geometry shader sources.
     *
     * @param renderable     The Renderable instance to be rendered by this shader.
     * @param config         The configuration for the geometry shader, containing shader settings and additional parameters.
     * @param prefix         A string prefix used to resolve shader source paths.
     * @param vertexShader   The name of the vertex shader file or source code to be used.
     * @param fragmentShader The name of the fragment shader file or source code to be used.
     */
    public WorldShader(final Renderable renderable, final Config config, final String prefix, final String vertexShader,
                       final String fragmentShader) {
        this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
    }

    /**
     * Constructs a new WorldShader instance with the specified renderable, configuration, and geometry shader program.
     *
     * @param renderable The Renderable instance to be rendered by this shader.
     * @param config The configuration for the geometry shader, containing shader settings and parameters.
     * @param shaderProgram The ShaderProgram instance containing the vertex, fragment, and geometry shader programs.
     */
    public WorldShader(Renderable renderable, Config config, ShaderProgram shaderProgram) {
        super(renderable, config, shaderProgram);

        this.u_globalSunlight = this.register(Inputs.globalSunlight, Setters.globalSunlight);
        this.u_atlasSize = this.register(Inputs.atlasSize, Setters.atlasSize);
        this.u_atlasOffset = this.register(Inputs.atlasOffset, Setters.atlasOffset);
        this.u_lod = this.register(Inputs.lod, Setters.lod);
        this.u_lodThreshold = this.register(Inputs.lodThreshold, Setters.lodThreshold);
        this.u_cameraUp0 = this.register(Inputs.cameraUp, Setters.cameraUp);
        this.u_fogColor = this.register(Inputs.fogColor, Setters.fogColor);
        this.u_chunkPosition = this.register(Inputs.chunkPosition, Setters.chunkPosition);
        this.u_shadowPixelSize = this.register(Inputs.shadowPixelSize, Setters.shadowPixelSize);
    }

    public static class Inputs extends DefaultShader.Inputs {
        public static final Uniform globalSunlight = new Uniform("u_globalSunlight");
        public static final Uniform atlasSize = new Uniform("u_atlasSize");
        public static final Uniform atlasOffset = new Uniform("u_atlasOffset");
        public static final Uniform lod = new Uniform("u_lod");
        public static final Uniform lodThreshold = new Uniform("u_lodThreshold");
        public static final Uniform cameraUp = new Uniform("u_cameraUp0");
        public static final Uniform fogColor = new Uniform("u_fogColor");
        public static final Uniform chunkPosition = new Uniform("u_chunkPosition");
        public static final Uniform shadowPixelSize = new Uniform("u_shadowPixelSize");

    }
    public static class Setters extends DefaultShader.Setters {
        public static final Setter globalSunlight = new GlobalSetter() {
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

        public static final Setter atlasSize = new GlobalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                Vec2f f = ClientWorld.ATLAS_SIZE.get().f();
                shader.set(inputID, new Vector2(f.x, f.y));
            }
        };

        public static final Setter atlasOffset = new GlobalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                Vec2f f = ClientWorld.ATLAS_OFFSET.get().f();
                shader.set(inputID, new Vector2(f.x, f.y));
            }
        };

        public static final Setter lodThreshold = new GlobalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, ClientConfiguration.lodThreshold.getValue());
            }
        };

        public static final Setter lod = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                if (renderable.userData instanceof ClientChunk) {
                    ClientChunk clientChunk = (ClientChunk) renderable.userData;
                    shader.set(inputID, clientChunk.lod);
                }
            }
        };

        public static final Setter cameraUp = new GlobalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, CAMERA_UP);
            }
        };

        public static final Setter fogColor = new GlobalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                WorldRenderer worldRenderer = QuantumClient.get().worldRenderer;
                if (worldRenderer != null) {
                    shader.set(inputID, worldRenderer.getFogColor());
                } else {
                    shader.set(inputID, Color.BLACK);
                }
            }
        };

        public static final Setter chunkPosition = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                Object userData = renderable.userData;
                if (userData instanceof Chunk) {
                    Chunk chunk = (Chunk) userData;
                    shader.set(inputID, chunk.vec.x, chunk.vec.y, chunk.vec.z);
                } else {
                    shader.set(inputID, 0, 0, 0);
                }
            }
        };

        public static final Setter shadowPixelSize = new GlobalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                if (QuantumClient.get().graphicsSetting.compareTo(GraphicsSetting.IMMERSIVE) >= 0.0) {
                    shader.set(inputID, 1f / TEXTURE_SIZE);
                }
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
            throw new GdxRuntimeException("Failed to initialize world shader", e);
        }
    }
}
