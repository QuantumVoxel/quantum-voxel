package com.ultreon.quantum.client.render.shader;

import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.common.base.Supplier;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.resources.ResourceFileHandle;
import com.ultreon.quantum.client.shaders.provider.ModelViewShaderProvider;
import com.ultreon.quantum.client.shaders.provider.SkyboxShaderProvider;
import com.ultreon.quantum.client.shaders.provider.WorldShaderProvider;

import static com.ultreon.quantum.client.QuantumClient.get;
import static com.ultreon.quantum.client.QuantumClient.id;

@SuppressWarnings("SameParameterValue")
public class Shaders {
    public static final Supplier<DepthShaderProvider> DEPTH = Shaders.register("depth", () -> new MyDepthShaderProvider(
            new ResourceFileHandle(id("shaders/depth.vert")),
            new ResourceFileHandle(id("shaders/depth.frag"))
    ));

    public static final Supplier<DefaultShaderProvider> DEFAULT = Shaders.register("default", MyDefaultShaderProvider::new);
    
    public static final Supplier<WorldShaderProvider> WORLD = Shaders.register("world", () -> new WorldShaderProvider(
            new ResourceFileHandle(id("shaders/world.vert")),
            new ResourceFileHandle(id("shaders/world.frag"))
    ));
    public static final Supplier<DefaultShaderProvider> SKYBOX = Shaders.register("skybox", () -> new SkyboxShaderProvider(
            new ResourceFileHandle(id("shaders/skybox.vert")),
            new ResourceFileHandle(id("shaders/skybox.frag"))
    ));
    public static final Supplier<ModelViewShaderProvider> MODEL_VIEW = Shaders.register("model_view", () -> new ModelViewShaderProvider(
            new ResourceFileHandle(id("shaders/model_view.vert")),
            new ResourceFileHandle(id("shaders/model_view.frag"))
    ));
    public static final Supplier<ModelViewShaderProvider> OUTLINE = Shaders.register("outline", () -> new ModelViewShaderProvider(
            new ResourceFileHandle(id("shaders/outline.vert")),
            new ResourceFileHandle(id("shaders/outline.frag"))
    ));

    private static <T extends ShaderProvider> Supplier<T> register(String name, Supplier<T> provider) {
        return get().getShaderProviderManager().register(id(name), provider);
    }

    public static void checkShaderCompilation(ShaderProgram program, String filename) {
        String shaderLog = program.getLog();
        if (program.isCompiled()) {
            if (shaderLog.isEmpty()) QuantumClient.LOGGER.debug("Shader compilation for {} success", filename);
            else QuantumClient.LOGGER.warn("Shader compilation warnings for {}:\n{}", filename, shaderLog);
        } else throw new GdxRuntimeException("Shader compilation failed for " + filename + ":\n" + shaderLog);
    }

    public static void init() {
        // NOOP
    }
}
