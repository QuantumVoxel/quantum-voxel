package dev.ultreon.hydro.engine;

import dev.ultreon.hydro.*;
import dev.ultreon.hydro.core.ShaderLanguage;
import dev.ultreon.hydro.graphics.ShaderPart;
import dev.ultreon.hydro.graphics.ShaderProgram;
import dev.ultreon.hydro.graphics.SpriteBatch;
import dev.ultreon.hydro.graphics.Stencils;
import org.jetbrains.annotations.Nullable;

public interface GraphicsEngine {
    PlatformType getType();

    void exit(int status);

    void halt();

    SpriteBatch createSpiteBatch();

    ShaderLanguage getShaderLanguage();

    /**
     * Creates a shader program from the specified paths.
     *
     * @param vertexPath   the path to the vertex shader
     * @param fragmentPath the path to the fragment shader
     * @return the created shader program
     */
    ShaderProgram createShaderProgram(String vertexPath, String fragmentPath);

    /**
     * Creates a shader program from the specified paths.
     *
     * @param vertexPath   the path to the vertex shader
     * @param geometryPath the path to the geometry shader
     * @param fragmentPath the path to the fragment shader
     * @return the created shader program
     * @throws UnsupportedOperationException if the platform doesn't support geometry shaders
     */
    ShaderProgram createShaderProgram(String vertexPath, String geometryPath, String fragmentPath);

    ShaderProgram createShaderProgram(ShaderPart[] parts);

    boolean isCustomShaderSupported();

    boolean isGeometrySupported();

    boolean isTessellationSupported();

    void clear(boolean clearColor, boolean clearDepth);

    void clearColor(float r, float g, float b, float a);

    void clearColor(float r, float g, float b);

    @Nullable Stencils getStencils();

    void openKeyboard(int x, int y, KeyboardType type);

    boolean isKeyboardTypeSupported(KeyboardType type);

    void closeKeyboard();

    void setVSync(boolean vsync);

    boolean isVSync();

    void setFullscreen(boolean fullscreen);

    boolean isFullscreen();

    boolean isWindowed();
}
