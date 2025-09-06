package dev.ultreon.hydro.backends.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import dev.ultreon.hydro.*;
import dev.ultreon.hydro.backends.libgdx.graphics.GdxShaderProgram;
import dev.ultreon.hydro.backends.libgdx.graphics.GdxSpriteBatch;
import dev.ultreon.hydro.backends.libgdx.graphics.GdxStencils;
import dev.ultreon.hydro.core.Application;
import dev.ultreon.hydro.core.ShaderLanguage;
import dev.ultreon.hydro.engine.GraphicsEngine;
import dev.ultreon.hydro.graphics.ShaderPart;
import dev.ultreon.hydro.graphics.ShaderProgram;
import dev.ultreon.hydro.graphics.SpriteBatch;
import dev.ultreon.hydro.graphics.Stencils;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class LibGDXGraphicsEngine implements GraphicsEngine {
    private final Application app;
    private boolean vsync;
    private float clearRed, clearGreen, clearBlue, clearAlpha;

    public LibGDXGraphicsEngine(Application app) {
        this.app = app;
    }

    @Override
    public PlatformType getType() {
        switch (SharedLibraryLoader.os) {
            case Windows:
                return PlatformType.Windows;
            case Linux:
                return PlatformType.Linux;
            case MacOsX:
                return PlatformType.MacOS;
            case Android:
                return PlatformType.Android;
            case IOS:
                return PlatformType.IOS;
            default:
                return PlatformType.Unknown;
        }
    }

    public void launch(Consumer<ApplicationListener> listener) {
        listener.accept(new ApplicationListener() {
            @Override
            public void create() {
                app.create();
            }

            @Override
            public void resize(int width, int height) {
                app.resize(width, height);
            }

            @Override
            public void render() {
                app.render();
            }

            @Override
            public void pause() {
                app.pause();
            }

            @Override
            public void resume() {
                app.resume();
            }

            @Override
            public void dispose() {
                app.destroy();
            }
        });
    }

    @Override
    public void exit(int status) {
        Gdx.app.exit();
        System.exit(status);
    }

    @Override
    public void halt() {
        Gdx.app.exit();
        System.exit(0);
    }

    @Override
    public SpriteBatch createSpiteBatch() {
        return new GdxSpriteBatch();
    }

    @Override
    public ShaderLanguage getShaderLanguage() {
        return ShaderLanguage.Glsl;
    }

    @Override
    public ShaderProgram createShaderProgram(String vertexPath, String fragmentPath) {
        return new GdxShaderProgram(vertexPath, fragmentPath);
    }

    @Override
    public ShaderProgram createShaderProgram(String vertexPath, String geometryPath, String fragmentPath) {
        throw new UnsupportedOperationException("Geometry shaders are not supported in libgdx.");
    }

    @Override
    public ShaderProgram createShaderProgram(ShaderPart[] parts) {
        return new GdxShaderProgram(parts);
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
        if (clearColor) {
            Gdx.gl.glClearColor(clearRed, clearGreen, clearBlue, clearAlpha);
        }
        int flags = 0;
        if (clearDepth) flags |= GL20.GL_DEPTH_BUFFER_BIT;
        if (clearColor) flags |= GL20.GL_COLOR_BUFFER_BIT;
        Gdx.gl.glClear(flags);
    }

    @Override
    public void clearColor(float r, float g, float b, float a) {
        clearRed = r;
        clearGreen = g;
        clearBlue = b;
        clearAlpha = a;
    }

    @Override
    public void clearColor(float r, float g, float b) {
        clearRed = r;
        clearGreen = g;
        clearBlue = b;
        clearAlpha = 1.0f;
    }

    @Override
    public @Nullable Stencils getStencils() {
        return new GdxStencils();
    }

    @Override
    public void openKeyboard(int x, int y, KeyboardType type) {
        Input.OnscreenKeyboardType onscreenKeyboardType = Input.OnscreenKeyboardType.Default;
        switch (type) {
            case Number:
                onscreenKeyboardType = Input.OnscreenKeyboardType.NumberPad;
                break;
            case Phone:
                onscreenKeyboardType = Input.OnscreenKeyboardType.PhonePad;
                break;
            case Email:
                onscreenKeyboardType = Input.OnscreenKeyboardType.Email;
                break;
            case Password:
                onscreenKeyboardType = Input.OnscreenKeyboardType.Password;
                break;
            case Url:
                onscreenKeyboardType = Input.OnscreenKeyboardType.URI;
                break;
            default:
                break;
        }

        Gdx.input.setOnscreenKeyboardVisible(true, onscreenKeyboardType);
    }

    @Override
    public boolean isKeyboardTypeSupported(KeyboardType type) {
        switch (type) {
            case Number:
            case Phone:
            case Email:
            case Password:
            case Url:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void closeKeyboard() {
        Gdx.input.setOnscreenKeyboardVisible(false);
    }

    @Override
    public void setVSync(boolean vsync) {
        Gdx.graphics.setVSync(vsync);
        this.vsync = vsync;
    }

    @Override
    public boolean isVSync() {
        return vsync;
    }

    @Override
    public void setFullscreen(boolean fullscreen) {
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    }

    @Override
    public boolean isFullscreen() {
        return Gdx.graphics.isFullscreen();
    }

    @Override
    public boolean isWindowed() {
        return !Gdx.graphics.isFullscreen();
    }
}
