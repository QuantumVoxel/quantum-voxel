package dev.ultreon.hydro.backends.opengl.engines;

import dev.ultreon.hydro.Hydro;
import dev.ultreon.hydro.core.Application;
import dev.ultreon.hydro.core.Destroyable;
import dev.ultreon.hydro.core.HydroException;
import dev.ultreon.hydro.core.HydroSettings;
import dev.ultreon.hydro.engine.InputEngine;
import org.lwjgl.glfw.GLFW;

public class GLFWWindow implements Destroyable {
    final Application app;
    private final long handle;
    private InputEngine inputEngine;

    public static void init() {
        if (!GLFW.glfwInit()) {
            throw new HydroException("GLFW could not be initialized!");
        }
    }

    public GLFWWindow(HydroSettings settings, Application app) {
        this.app = app;
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        handle = GLFW.glfwCreateWindow(settings.width, settings.height, settings.title, 0, 0);
        if (handle == 0) {
            throw new HydroException("GLFW window could not be created!");
        }

        GLFW.glfwMakeContextCurrent(handle);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(handle);
    }

    public void mainLoop() {
        Hydro.input = inputEngine = new GLFWInputEngine(this);

        app.create();
        do {
            app.render();
        } while (update());
        destroy();
    }

    public long getHandle() {
        return handle;
    }

    public void destroy() {
        app.destroy();
        GLFW.glfwDestroyWindow(handle);
        GLFW.glfwTerminate();
        app.onExit(0);
    }

    public boolean update() {
        GLFW.glfwSwapBuffers(GLFW.glfwGetCurrentContext());
        GLFW.glfwPollEvents();

        if (!GLFW.glfwWindowShouldClose(GLFW.glfwGetCurrentContext())) return true;
        if (app.onCloseRequested()) GLFW.glfwSetWindowShouldClose(GLFW.glfwGetCurrentContext(), false);

        return false;
    }

    public InputEngine getInputEngine() {
        return inputEngine;
    }
}
