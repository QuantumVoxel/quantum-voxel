package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.util.Identifier;

import java.util.function.Supplier;

public class ShaderPrograms {
    public static final Supplier<ShaderProgram> XOR = ShaderPrograms.register("xor");
    public static final Supplier<ShaderProgram> OUTLINE = ShaderPrograms.register("outline");
    public static final Supplier<ShaderProgram> MODEL = ShaderPrograms.register("model");
    public static final Supplier<ShaderProgram> DEFAULT = ShaderPrograms.register("default");
    public static final Supplier<ShaderProgram> DEPTH = ShaderPrograms.register("depth");
    public static final Supplier<ShaderProgram> WORLD = ShaderPrograms.register("world");
    public static final Supplier<ShaderProgram> SKYBOX = ShaderPrograms.register("skybox");

    private static Supplier<ShaderProgram> register(String name) {
        return QuantumClient.get().getShaderProgramManager().register(QuantumClient.id(name), () -> {
            Identifier id = QuantumClient.id(name);

            try {
                return ShaderPrograms.createShader(id);
            } catch (GdxRuntimeException e) {
                throw new RuntimeException("Failed to create shader program: " + id, e);
            }
        });
    }

    public static ShaderProgram createShader(Identifier id) {
        ResourceFileHandle vertexResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".vert"));
        ResourceFileHandle fragmentResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".frag"));

        ShaderProgram program = new ShaderProgram(vertexResource, fragmentResource);
        String shaderLog = program.getLog();
        if (program.isCompiled()) {
            if (shaderLog.isEmpty()) QuantumClient.LOGGER.debug("Shader compilation success");
            else QuantumClient.LOGGER.warn("Shader compilation warnings:\n%s", shaderLog);
        } else throw new GdxRuntimeException("Shader compilation failed:\n" + shaderLog);
        return program;
    }

    public static void init() {
        // NOOP
    }
}
