package dev.ultreon.quantum.client.shaders;

import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.shaders.provider.*;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.function.Supplier;

import static dev.ultreon.quantum.client.QuantumClient.get;

/**
 * The Shaders class provides a collection of shader providers for the game.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
@SuppressWarnings("SameParameterValue")
public class Shaders {
    /**
     * The depth shader provider. Located in {@code shaders/depth.vert} and {@code shaders/depth.frag}.
     * 
     * @see DepthShaderProvider
     */
    public static final Supplier<DepthShaderProvider> DEPTH = Shaders.register("depth", () -> new MyDepthShaderProvider(
            QuantumClient.shader(NamespaceID.of("depth.vert")),
            QuantumClient.shader(NamespaceID.of("depth.frag"))
    ));

    /**
     * The default shader provider. Located in {@code shaders/default.vert} and {@code shaders/default.frag}.
     * 
     * @see DefaultShaderProvider
     */
    public static final Supplier<DefaultShaderProvider> DEFAULT = Shaders.register("default", MyDefaultShaderProvider::new);

    /**
     * The scene shader provider. Located in {@code shaders/scene.vert}, {@code shaders/scene.frag}.
     * 
     * @see WorldShaders
     */
    public static final Supplier<WorldShaders> WORLD = Shaders.register("world", () -> new WorldShaders(
            QuantumClient.resource(NamespaceID.of("shaders/scene.vert")),
            QuantumClient.resource(NamespaceID.of("shaders/scene.frag"))
    ));

    /**
     * The scene shader provider.
     * Located in {@code shaders/transparent.vert}, {@code shaders/transparent.frag}.
     *
     * @see WorldShaders
     */
    public static final Supplier<WorldShaders> TRANSPARENT = Shaders.register("transparent", () -> new WorldShaders(
            QuantumClient.resource(NamespaceID.of("shaders/transparent.vert")),
            QuantumClient.resource(NamespaceID.of("shaders/transparent.frag"))));

    /**
     * The scene shader provider.
     * Located in {@code shaders/water.vert}, {@code shaders/water.frag}.
     *
     * @see WorldShaders
     */
    public static final Supplier<WorldShaders> WATER = Shaders.register("water", () -> new WorldShaders(
            QuantumClient.resource(NamespaceID.of("shaders/water.vert")),
            QuantumClient.resource(NamespaceID.of("shaders/water.frag"))));

    /**
     * The scene shader provider.
     * Located in {@code shaders/cutout.vert}, {@code shaders/cutout.frag}.
     *
     * @see WorldShaders
     */
    public static final Supplier<WorldShaders> CUTOUT = Shaders.register("cutout", () -> new WorldShaders(
            QuantumClient.resource(NamespaceID.of("shaders/cutout.vert")),
            QuantumClient.resource(NamespaceID.of("shaders/cutout.frag"))
    ));

    /**
     * The scene shader provider.
     * Located in {@code shaders/foliage.vert}, {@code shaders/foliage.frag}.
     *
     * @see WorldShaders
     */
    public static final Supplier<WorldShaders> FOLIAGE = Shaders.register("foliage", () -> new WorldShaders(
            QuantumClient.resource(NamespaceID.of("shaders/foliage.vert")),
            QuantumClient.resource(NamespaceID.of("shaders/foliage.frag"))
    ));

    /**
     * The skybox shader provider. Located in {@code shaders/skybox.vert} and {@code shaders/skybox.frag}.
     */
    public static final Supplier<DefaultShaderProvider> SKYBOX = Shaders.register("skybox", () -> new SkyboxShaders(
            QuantumClient.shader(NamespaceID.of("skybox.vert")),
            QuantumClient.shader(NamespaceID.of("skybox.frag"))
    ));

    /**
     * The model view shader provider. Located in {@code shaders/model.vert}, {@code shaders/model.frag}, and {@code shaders/model.geom}.
     * 
     * @see ModelShaders
     */
    public static final Supplier<ModelShaders> MODEL_VIEW = Shaders.register("model_view", () -> new ModelShaders(
            QuantumClient.shader(NamespaceID.of("model.vert")),
            QuantumClient.shader(NamespaceID.of("model.frag"))));

    /**
     * The gizmo shader provider. Located in {@code shaders/gizmo.vert}, {@code shaders/gizmo.frag}, and {@code shaders/gizmo.geom}.
     * 
     * @see GizmoShaders
     */
    public static final Supplier<GizmoShaders> GIZMO = Shaders.register("gizmo", () -> new GizmoShaders(
            QuantumClient.shader(NamespaceID.of("gizmo.vert")),
            QuantumClient.shader(NamespaceID.of("gizmo.frag")),
            QuantumClient.shader(NamespaceID.of("gizmo.geom"))));

    /**
     * The gizmo outline shader provider. Located in {@code shaders/gizmo_outline.vert} and {@code shaders/gizmo_outline.frag}.
     * 
     * @see GizmoOutlineShaders
     */
    public static final Supplier<GizmoOutlineShaders> GIZMO_OUTLINE = Shaders.register("gizmo_outline", () -> new GizmoOutlineShaders(
            QuantumClient.shader(NamespaceID.of("gizmo_outline.vert")),
            QuantumClient.shader(NamespaceID.of("gizmo_outline.frag"))
    ));

    /**
     * Registers a shader provider with the given name.
     * 
     * @param name The name of the shader provider.
     * @param provider The shader provider.
     * @return The registered shader provider.
     */
    private static <T extends ShaderProvider> Supplier<T> register(String name, Supplier<T> provider) {
        return get().getShaderProviderManager().register(NamespaceID.of(name), provider);
    }

    /**
     * Checks if a shader has been compiled successfully.
     * 
     * @param program The shader program.
     * @param filename The filename of the shader.
     */
    public static void checkShaderCompilation(ShaderProgram program, String filename) {
        String shaderLog = program.getLog();
//        if (program.isCompiled()) {
            if (shaderLog.isEmpty()) QuantumClient.LOGGER.debug("Shader compilation for {} success", filename);
            else QuantumClient.LOGGER.warn("Shader compilation warnings for {}:\n{}", filename, shaderLog);
//        } else {
//            throw new GdxRuntimeException("Shader compilation failed for " + filename + ":\n" + shaderLog);
//        }
    }

    public static void init() {
        // NOOP
    }
}
