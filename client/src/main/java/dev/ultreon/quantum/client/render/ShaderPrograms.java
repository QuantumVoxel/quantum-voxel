package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.ultreon.mixinprovider.GeomShaderProgram;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.util.Identifier;

import java.util.function.Supplier;

public class ShaderPrograms {
    public static final Supplier<ShaderProgram> XOR = ShaderPrograms.register("xor");
    public static final Supplier<ShaderProgram> OUTLINE = ShaderPrograms.register("outline");
    public static final Supplier<ShaderProgram> MAIN = ShaderPrograms.register("main");
    public static final Supplier<ShaderProgram> DEFAULT = ShaderPrograms.register("default");
    public static final Supplier<ShaderProgram> DEPTH = ShaderPrograms.register("depth");
    public static final Supplier<ShaderProgram> SCENE = ShaderPrograms.register("scene");
    public static final Supplier<ShaderProgram> SKYBOX = ShaderPrograms.register("skybox");
    public static final Supplier<ShaderProgram> EFFECT_CUTOUT = ShaderPrograms.register("effect_cutout");
    public static final Supplier<ShaderProgram> EFFECT_TRANSPARENT = ShaderPrograms.register("effect_transparent");
    public static final Supplier<ShaderProgram> EFFECT_GENERIC = ShaderPrograms.register("effect_generic");

    private static Supplier<ShaderProgram> register(String name) {
        return QuantumClient.get().getShaderProgramManager().register(QuantumClient.id(name), () -> {
            Identifier id = QuantumClient.id(name);
            return ShaderPrograms.createShader(id);
        });
    }

    public static ShaderProgram createShader(Identifier id) {
        if (!QuantumClient.isOnMainThread()) return QuantumClient.invokeAndWait(() -> createShader(id));

        ResourceFileHandle vertexResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".vert"));
        ResourceFileHandle geometryResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".geom"));
        ResourceFileHandle fragmentResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".frag"));

        ShaderProgram program = geometryResource.exists()
                ? new GeomShaderProgram(vertexResource, fragmentResource, geometryResource)
                : new ShaderProgram(vertexResource, fragmentResource);

        String shaderLog = program.getLog();
        if (program.isCompiled()) {
            if (shaderLog.isEmpty()) QuantumClient.LOGGER.debug("Shader compilation success for " + id);
            else QuantumClient.LOGGER.warn("Shader compilation warnings for " + id + ":\n{}", shaderLog);
        } else {
            QuantumClient.LOGGER.error("Shader compilation failed for " + id + ":\n" + shaderLog);
        }
        return program;
    }

    public static void init() {
        // NOOP
    }
}
