package dev.ultreon.hydro.engine;

import dev.ultreon.hydro.fs.PathHandle;

public interface IOEngine {
    void exit(int status);

    PathHandle getWorkingDirectory();

    PathHandle asset(String path);

    PathHandle data(String path);

    PathHandle cache(String path);

    PathHandle config(String path);

    PathHandle external(String path);

    PathHandle absolute(String path);

    String getWindowTitle();

    void setWindowTitle(String title);

    void setWindowIcon(PathHandle path);

    void setWindowResizable(boolean resizable);

    void setWindowSize(int width, int height);

    void setWindowPosition(int x, int y);

    void setWindowVSync(boolean vsync);

    void setWindowFullscreen(boolean fullscreen);

    boolean isWindowFullscreen();
}
