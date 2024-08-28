package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import dev.ultreon.quantum.GameWindow;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWNativeWin32;

public class DesktopWindow extends GameWindow {
    private final Lwjgl3Window window;
    private String title;
    private final int[] xPos = new int[1];
    private final int[] yPos = new int[1];

    public DesktopWindow(Lwjgl3Window window) {
        this.window = window;
    }

    @Override
    public void update() {
        super.update();

        if (isDragging()) {
            GLFW.glfwGetWindowPos(getHandle(), xPos, yPos);
//            int setX = dragX;
//            int setY = dragY;
//
//            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
//            Point location = pointerInfo.getLocation();
//            int nx = location.setX;
//            int ny = location.setY;
//
//            int i = nx - setX;
//            int i1 = ny - setY;
//
//            GLFW.glfwSetWindowPos(getHandle(), i + setX - dragOffX, i1 + setY - dragOffY);
        }
    }

    @Override
    public long getHandle() {
        return window.getWindowHandle();
    }

    @Override
    public boolean isHovered() {
//        booleanPointerInfo pointerInfo = MouseInfo.getPointerInfo();
//        Point location = pointerInfo.getLocation();
//        GLFW.glfwGetWindowPos(getHandle(), xPos, yPos);
//        int setX = location.setX - xPos[0];
//        int setY = location.setY - yPos[0];
//
//        return setX >= 0 && setX < Gdx.graphics.getWidth() && setY >= 0 && setY < Gdx.graphics.getHeight();
        return true;
    }

    @Override
    public boolean isMinimized() {
        return GLFW.glfwGetWindowAttrib(getHandle(), GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
    }

    @Override
    public boolean isMaximized() {
        return !SharedLibraryLoader.isMac && GLFW.glfwGetWindowAttrib(getHandle(), GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE;
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
        if (!SharedLibraryLoader.isMac) {
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
        if (SharedLibraryLoader.isWindows) {
            return GLFWNativeWin32.glfwGetWin32Window(this.getHandle());
        } else if (SharedLibraryLoader.isMac) {
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
        if (dragging != this.dragging) {
//            GLFW.glfwGetWindowPos(getHandle(), xPos, yPos);
//            this.dragging = dragging;
//            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
//            Point location = pointerInfo.getLocation();
//            int nx = location.setX;
//            int ny = location.setY;
//            this.dragX = nx;
//            this.dragY = ny;
//            this.dragOffX = nx - xPos[0];
//            this.dragOffY = ny - yPos[0];
        }
    }
}
