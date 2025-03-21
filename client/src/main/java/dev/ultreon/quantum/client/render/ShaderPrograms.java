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
     * The XOR shader program. Located in the `shaders/xor.vert` and `shaders/xor.frag` files.
     */
    public static final Supplier<ShaderProgram> XOR = ShaderPrograms.register("xor");
    /**
     * The outline shader program. Located in the `shaders/outline.vert` and `shaders/outline.frag` files.
     */
    public static final Supplier<ShaderProgram> OUTLINE = ShaderPrograms.register("outline");
    /**
     * The main shader program. Located in the `shaders/main.vert` and `shaders/main.frag` files.
     */
    public static final Supplier<ShaderProgram> MAIN = ShaderPrograms.register("main");
    /**
     * The default shader program. Located in the `shaders/default.vert` and `shaders/default.frag` files.
     */
    public static final Supplier<ShaderProgram> DEFAULT = ShaderPrograms.register("default");
    /**
     * The depth shader program. Located in the `shaders/depth.vert` and `shaders/depth.frag` files.
     */
    public static final Supplier<ShaderProgram> DEPTH = ShaderPrograms.register("depth");
    /**
     * The scene shader program. Located in the `shaders/scene.vert` and `shaders/scene.frag` files.
     */
    public static final Supplier<ShaderProgram> SCENE = ShaderPrograms.register("scene");
    /**
     * The skybox shader program. Located in the `shaders/skybox.vert` and `shaders/skybox.frag` files.
     */
    public static final Supplier<ShaderProgram> SKYBOX = ShaderPrograms.register("skybox");

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
