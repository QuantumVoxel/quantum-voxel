package dev.ultreon.quantum.client.render;

import dev.ultreon.quantum.client.shaders.provider.GameShaders;

/**
 * The ShaderContext class is used to manage the shader context.
 * <p>
 * This is a part of the render pipeline. It is used to manage the shader context.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class ShaderContext {
    private static GameShaders mode;

    /**
     * Sets the shader mode.
     * 
     * @param mode The shader mode.
     */
    public static void set(GameShaders mode) {
        ShaderContext.mode = mode;
    }

    /**
     * Gets the shader mode.
     * 
     * @return The shader mode.
     */
    public static GameShaders get() {
        return ShaderContext.mode;
    }

    /**
     * The shader mode enum.
     */
    public enum ShaderMode {
        /**
         * The depth shader mode.
         */
        DEPTH,
        
        /**
         * The diffuse shader mode.
         */
        DIFFUSE,

        /**
         * The normal shader mode.
         */
        NORMAL,

        /**
         * The specular shader mode.
         */
        SPECULAR,

        /**
         * The emissive shader mode.
         */
        EMISSIVE,            
    }
}
