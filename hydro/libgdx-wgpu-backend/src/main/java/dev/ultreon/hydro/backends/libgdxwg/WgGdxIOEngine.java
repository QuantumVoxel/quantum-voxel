package dev.ultreon.hydro.backends.libgdxwg;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import dev.ultreon.hydro.backends.libgdxwg.fs.WgGdxPathHandle;
import dev.ultreon.hydro.core.Application;
import dev.ultreon.hydro.engine.IOEngine;
import dev.ultreon.hydro.fs.PathHandle;

public class WgGdxIOEngine implements IOEngine {
    private final Application app;
    private String windowTitle = "Hydro";

    public WgGdxIOEngine(Application app) {
        this.app = app;
    }

    @Override
    public void exit(int status) {
        app.onExit(status);
        Gdx.app.exit();
        System.exit(status);
    }

    @Override
    public PathHandle getWorkingDirectory() {
        if (System.getProperty("user.dir") != null) {
            return absolute(System.getProperty("user.dir"));
        }
        return data(".");
    }

    @Override
    public PathHandle asset(String path) {
        return new WgGdxPathHandle(path, Files.FileType.Internal);
    }

    @Override
    public PathHandle data(String path) {
        return new WgGdxPathHandle(path, Files.FileType.Local).resolve("data");
    }

    @Override
    public PathHandle cache(String path) {
        return new WgGdxPathHandle(path, Files.FileType.Local).resolve("cache");
    }

    @Override
    public PathHandle config(String path) {
        return new WgGdxPathHandle(path, Files.FileType.Local).resolve("config");
    }

    @Override
    public PathHandle external(String path) {
        return new WgGdxPathHandle(path, Files.FileType.External);
    }

    @Override
    public PathHandle absolute(String path) {
        return new WgGdxPathHandle(path, Files.FileType.Absolute);
    }

    @Override
    public String getWindowTitle() {
        return windowTitle;
    }

    @Override
    public void setWindowTitle(String title) {
        Gdx.graphics.setTitle(title);
        windowTitle = title;
    }

    @Override
    public void setWindowIcon(PathHandle path) {
        // Can't do this in libgdx
    }

    @Override
    public void setWindowResizable(boolean resizable) {
        Gdx.graphics.setResizable(resizable);
    }

    @Override
    public void setWindowSize(int width, int height) {
        Gdx.graphics.setWindowedMode(width, height);
    }

    @Override
    public void setWindowPosition(int x, int y) {
        // Can't do this in libgdx
    }

    @Override
    public void setWindowVSync(boolean vsync) {
        Gdx.graphics.setVSync(vsync);
    }

    @Override
    public void setWindowFullscreen(boolean fullscreen) {
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    }

    @Override
    public boolean isWindowFullscreen() {
        return Gdx.graphics.isFullscreen();
    }
}
