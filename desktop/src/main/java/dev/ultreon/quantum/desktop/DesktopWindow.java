package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import dev.ultreon.quantum.GameWindow;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class DesktopWindow extends GameWindow {
    private final Lwjgl3Window window;

    public DesktopWindow(Lwjgl3Window window) {
        this.window = window;
    }

    @Override
    public long getHandle() {
        return window.getWindowHandle();
    }

    @Override
    public boolean isHovered() {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        Point location = pointerInfo.getLocation();
        int x = location.x;
        int y = location.y;

        return x >= 0 && x < Gdx.graphics.getWidth() && y >= 0 && y < Gdx.graphics.getHeight();
    }

    @Override
    public boolean isMinimized() {
        return GLFW.glfwGetWindowAttrib(getHandle(), GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
    }

    @Override
    public boolean isMaximized() {
        return GLFW.glfwGetWindowAttrib(getHandle(), GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE;
    }

    @Override
    public boolean isFocused() {
        return GLFW.glfwGetWindowAttrib(getHandle(), GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
    }

    @Override
    public void close() {
        GLFW.glfwSetWindowShouldClose(getHandle(), true);
    }

    @Override
    public void minimize() {
        GLFW.glfwIconifyWindow(getHandle());
    }

    @Override
    public void maximize() {
        GLFW.glfwMaximizeWindow(getHandle());
    }

    @Override
    public void restore() {
        GLFW.glfwRestoreWindow(getHandle());
    }

    @Override
    public void focus() {
        GLFW.glfwFocusWindow(getHandle());
    }

    @Override
    public void requestAttention() {
        GLFW.glfwRequestWindowAttention(getHandle());
    }

    @Override
    public void setResizable(boolean resizable) {
        GLFW.glfwSetWindowAttrib(getHandle(), GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
    }
}
