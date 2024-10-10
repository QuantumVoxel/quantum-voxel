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

@SuppressWarnings("SameParameterValue")
public class Shaders {
    public static final Supplier<DepthShaderProvider> DEPTH = Shaders.register("depth", () -> new MyDepthShaderProvider(
            QuantumClient.shader(id("depth.vert")),
            QuantumClient.shader(id("depth.frag"))
    ));

    public static final Supplier<DefaultShaderProvider> DEFAULT = Shaders.register("default", MyDefaultShaderProvider::new);
    
    public static final Supplier<SceneShaders> SCENE = Shaders.register("world", () -> new SceneShaders(
            QuantumClient.resource(id("shaders/scene.vert")),
            QuantumClient.resource(id("shaders/scene.frag")),
            QuantumClient.resource(id("shaders/scene.geom"))));
    public static final Supplier<DefaultShaderProvider> SKYBOX = Shaders.register("skybox", () -> new SkyboxShaders(
            QuantumClient.shader(id("skybox.vert")),
            QuantumClient.shader(id("skybox.frag"))
    ));
    public static final Supplier<DefaultShaderProvider> SKYBOX_DEBUG = Shaders.register("skybox_debug", () -> new SkyboxShaders(
            QuantumClient.shader(id("skybox.vert")),
            QuantumClient.shader(id("skybox_debug.frag"))
    ));
    public static final Supplier<ModelShaders> MODEL_VIEW = Shaders.register("model_view", () -> new ModelShaders(
            QuantumClient.shader(id("model.vert")),
            QuantumClient.shader(id("model.frag")),
            QuantumClient.shader(id("model.geom"))));
    public static final Supplier<GizmoShaders> GIZMO = Shaders.register("gizmo", () -> new GizmoShaders(
            QuantumClient.shader(id("gizmo.vert")),
            QuantumClient.shader(id("gizmo.frag")),
            QuantumClient.shader(id("gizmo.geom"))));
    public static final Supplier<OutlineShaderProvider> OUTLINE = Shaders.register("outline", () -> new OutlineShaderProvider(
            QuantumClient.shader(id("outline.vert")),
            QuantumClient.shader(id("outline.frag"))));
    public static final Supplier<GizmoOutlineShaders> GIZMO_OUTLINE = Shaders.register("gizmo_outline", () -> new GizmoOutlineShaders(
            QuantumClient.shader(id("gizmo_outline.vert")),
            QuantumClient.shader(id("gizmo_outline.frag"))
    ));

    private static <T extends ShaderProvider> Supplier<T> register(String name, Supplier<T> provider) {
        return get().getShaderProviderManager().register(id(name), provider);
    }

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
