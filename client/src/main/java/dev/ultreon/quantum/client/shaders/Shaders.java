package dev.ultreon.quantum.client.shaders;

import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.google.common.base.Supplier;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.client.shaders.provider.*;

import static dev.ultreon.quantum.client.QuantumClient.get;
import static dev.ultreon.quantum.client.QuantumClient.id;

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
            QuantumClient.shader(id("depth.vert")),
            QuantumClient.shader(id("depth.frag"))
    ));

    /**
     * The default shader provider. Located in {@code shaders/default.vert} and {@code shaders/default.frag}.
     * 
     * @see DefaultShaderProvider
     */
    public static final Supplier<DefaultShaderProvider> DEFAULT = Shaders.register("default", MyDefaultShaderProvider::new);

    /**
     * The scene shader provider. Located in {@code shaders/scene.vert}, {@code shaders/scene.frag}, and {@code shaders/scene.geom}.
     * 
     * @see SceneShaders
     */
    public static final Supplier<SceneShaders> SCENE = Shaders.register("world", () -> new SceneShaders(
            QuantumClient.resource(id("shaders/scene.vert")),
            QuantumClient.resource(id("shaders/scene.frag")),
            QuantumClient.resource(id("shaders/scene.geom"))));

    /**
     * The skybox shader provider. Located in {@code shaders/skybox.vert} and {@code shaders/skybox.frag}.
     */
    public static final Supplier<DefaultShaderProvider> SKYBOX = Shaders.register("skybox", () -> new SkyboxShaders(
            QuantumClient.shader(id("skybox.vert")),
            QuantumClient.shader(id("skybox.frag"))
    ));

    /**
     * The skybox debug shader provider. Located in {@code shaders/skybox.vert} and {@code shaders/skybox_debug.frag}.
     */
    public static final Supplier<DefaultShaderProvider> SKYBOX_DEBUG = Shaders.register("skybox_debug", () -> new SkyboxShaders(
            QuantumClient.shader(id("skybox.vert")),
            QuantumClient.shader(id("skybox_debug.frag"))
    ));

    /**
     * The model view shader provider. Located in {@code shaders/model.vert}, {@code shaders/model.frag}, and {@code shaders/model.geom}.
     * 
     * @see ModelShaders
     */
    public static final Supplier<ModelShaders> MODEL_VIEW = Shaders.register("model_view", () -> new ModelShaders(
            QuantumClient.shader(id("model.vert")),
            QuantumClient.shader(id("model.frag")),
            QuantumClient.shader(id("model.geom"))));

    /**
     * The gizmo shader provider. Located in {@code shaders/gizmo.vert}, {@code shaders/gizmo.frag}, and {@code shaders/gizmo.geom}.
     * 
     * @see GizmoShaders
     */
    public static final Supplier<GizmoShaders> GIZMO = Shaders.register("gizmo", () -> new GizmoShaders(
            QuantumClient.shader(id("gizmo.vert")),
            QuantumClient.shader(id("gizmo.frag")),
            QuantumClient.shader(id("gizmo.geom"))));

    /**
     * The outline shader provider. Located in {@code shaders/outline.vert} and {@code shaders/outline.frag}.
     * 
     * @see OutlineShaderProvider
     */
    public static final Supplier<OutlineShaderProvider> OUTLINE = Shaders.register("outline", () -> new OutlineShaderProvider(
            QuantumClient.shader(id("outline.vert")),
            QuantumClient.shader(id("outline.frag"))));

    /**
     * The gizmo outline shader provider. Located in {@code shaders/gizmo_outline.vert} and {@code shaders/gizmo_outline.frag}.
     * 
     * @see GizmoOutlineShaders
     */
    public static final Supplier<GizmoOutlineShaders> GIZMO_OUTLINE = Shaders.register("gizmo_outline", () -> new GizmoOutlineShaders(
            QuantumClient.shader(id("gizmo_outline.vert")),
            QuantumClient.shader(id("gizmo_outline.frag"))
    ));

    /**
     * Registers a shader provider with the given name.
     * 
     * @param name The name of the shader provider.
     * @param provider The shader provider.
     * @return The registered shader provider.
     */
    private static <T extends ShaderProvider> Supplier<T> register(String name, Supplier<T> provider) {
        return get().getShaderProviderManager().register(id(name), provider);
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
