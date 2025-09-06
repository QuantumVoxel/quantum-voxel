package dev.ultreon.hydro.backends.opengl.engines;

import dev.ultreon.hydro.KeyboardType;
import dev.ultreon.hydro.PlatformType;
import dev.ultreon.hydro.core.ShaderLanguage;
import dev.ultreon.hydro.engine.GraphicsEngine;
import dev.ultreon.hydro.graphics.ShaderPart;
import dev.ultreon.hydro.graphics.ShaderProgram;
import dev.ultreon.hydro.graphics.SpriteBatch;
import dev.ultreon.hydro.graphics.Stencils;
import org.jetbrains.annotations.Nullable;

public class GLGraphicsEngine implements GraphicsEngine {
    @Override
    public PlatformType getType() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return PlatformType.Windows;
        } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            return PlatformType.Linux;
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            return PlatformType.MacOS;
        } else {
            return PlatformType.Unknown;
        }
    }

    @Override
    public void exit(int status) {
        System.exit(status);
    }

    @Override
    public void halt() {
        Runtime.getRuntime().halt(0);
    }

    @Override
    public SpriteBatch createSpiteBatch() {
        return new GLSpriteBatch();
    }

    @Override
    public ShaderLanguage getShaderLanguage() {
        return null;
    }

    @Override
    public ShaderProgram createShaderProgram(String vertexPath, String fragmentPath) {
        return null;
    }

    @Override
    public ShaderProgram createShaderProgram(String vertexPath, String geometryPath, String fragmentPath) {
        return null;
    }

    @Override
    public ShaderProgram createShaderProgram(ShaderPart[] parts) {
        return null;
    }

    @Override
    public boolean isGeometrySupported() {
        return false;
    }

    @Override
    public boolean isTessellationSupported() {
        return false;
    }

    @Override
    public void clear(boolean clearColor, boolean clearDepth) {

    }

    @Override
    public void clearColor(float r, float g, float b, float a) {

    }

    @Override
    public void clearColor(float r, float g, float b) {

    }

    @Override
    public @Nullable Stencils getStencils() {
        return null;
    }

    @Override
    public void openKeyboard(int x, int y, KeyboardType type) {

    }

    @Override
    public boolean isKeyboardTypeSupported(KeyboardType type) {
        return false;
    }

    @Override
    public void closeKeyboard() {

    }

    @Override
    public void setVSync(boolean vsync) {

    }

    @Override
    public boolean isVSync() {
        return false;
    }

    @Override
    public void setFullscreen(boolean fullscreen) {

    }

    @Override
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public boolean isWindowed() {
        return false;
    }
}
