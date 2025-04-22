package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWNativeWin32;

public class DesktopGLWindow extends DesktopWindow {
    private final Lwjgl3Window window;
    private String title;
    private final int[] xPos = new int[1];
    private final int[] yPos = new int[1];

    public DesktopGLWindow(Lwjgl3Window window) {
        this.window = window;
    }

    @Override
    public void update() {
        super.update();

        if (isDragging()) {
            GLFW.glfwGetWindowPos(getHandle(), xPos, yPos);
        }
    }

    @Override
    public long getHandle() {
        return window.getWindowHandle();
    }

    @Override
    public boolean isHovered() {
        return true;
    }

    @Override
    public boolean isMinimized() {
        return GLFW.glfwGetWindowAttrib(getHandle(), GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
    }

    @Override
    public boolean isMaximized() {
        return !System.getProperty("os.name").equals("Mac OS X") && GLFW.glfwGetWindowAttrib(getHandle(), GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE;
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
    public void setVisible(boolean visible) {
        if (visible) {
            GLFW.glfwShowWindow(getHandle());
        } else {
            GLFW.glfwHideWindow(getHandle());
        }
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
        if (!System.getProperty("os.name").equals("Mac OS X")) {
            GLFW.glfwSetWindowAttrib(getHandle(), GLFW.GLFW_MAXIMIZED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowAttrib(getHandle(), GLFW.GLFW_ICONIFIED, GLFW.GLFW_FALSE);
        }

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

    @Override
    public long getPeer() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return GLFWNativeWin32.glfwGetWin32Window(this.getHandle());
        } else if (System.getProperty("os.name").startsWith("Mac")) {
            return GLFWNativeCocoa.glfwGetCocoaWindow(this.getHandle());
        } else {
            return -1L;
        }
    }

    @Override
    public void setTitle(String title) {
        Gdx.graphics.setTitle(title);
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setDragging(boolean dragging) {
        // no-op
    }
}
