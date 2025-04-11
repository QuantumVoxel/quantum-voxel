package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.function.Supplier;

/**
 * The ShaderPrograms class is used to manage the shader programs.
 * <p>
 * This is a part of the render pipeline. It is used to manage the shader programs.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class ShaderPrograms {

    /**
     * Registers a shader program.
     * 
     * @param name The name of the shader program.
     * @return The shader program.
     */
    private static Supplier<ShaderProgram> register(String name) {
        return QuantumClient.get().getShaderProgramManager().register(QuantumClient.id(name), () -> {
            NamespaceID id = QuantumClient.id(name);
            return ShaderPrograms.createShader(id);
        });
    }

    /**
     * Creates a shader program.
     * 
     * @param id The namespace ID of the shader program.
     * @return The shader program.
     */
    public static ShaderProgram createShader(NamespaceID id) {
        if (!QuantumClient.isOnRenderThread()) return QuantumClient.invokeAndWait(() -> createShader(id));

        ResourceFileHandle vertexResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".vert"));
        ResourceFileHandle fragmentResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".frag"));

        ShaderProgram program = new ShaderProgram(vertexResource, fragmentResource);
        String shaderLog = program.getLog();
        if (program.isCompiled()) {
            if (shaderLog.isEmpty()) QuantumClient.LOGGER.debug("Shader compilation success for {}", id);
            else QuantumClient.LOGGER.warn("Shader compilation warnings for {}:\n{}", id, shaderLog);
        } else {
            QuantumClient.LOGGER.error("Shader compilation failed for {}:\n{}", id, shaderLog);
        }
        return program;
    }

    /**
     * Initializes the shader programs.
     */
    public static void init() {
        // NOOP
    }
}
