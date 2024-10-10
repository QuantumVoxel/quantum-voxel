package dev.ultreon.quantum.desktop;


import com.github.dgzt.gdx.lwjgl3.Lwjgl3Window;
import dev.ultreon.mixinprovider.PlatformOS;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWNativeWin32;

public class DesktopVulkanWindow extends DesktopWindow {
    private final Lwjgl3Window window;
    private String title;

    public DesktopVulkanWindow(Lwjgl3Window window) {
        super();
        this.window = window;
    }

    @Override
    public boolean isHovered() {
        return window.isFocused();
    }

    @Override
    public boolean isFocused() {
        return window.isFocused();
    }

    @Override
    public boolean isMaximized() {
        return false;
    }

    @Override
    public boolean isMinimized() {
        return window.isIconified();
    }

    @Override
    public void setVisible(boolean visible) {
        window.setVisible(visible);
    }

    @Override
    public void close() {
        window.flash();
    }

    @Override
    public void requestAttention() {
        window.flash();
    }

    @Override
    public void focus() {
        window.focusWindow();
    }

    @Override
    public void minimize() {
        window.iconifyWindow();
    }

    @Override
    public void maximize() {
        window.maximizeWindow();
    }

    @Override
    public void restore() {
        window.restoreWindow();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        window.setTitle(title);
    }

    @Override
    public boolean isDragging() {
        return false;
    }

    @Override
    public void setDragging(boolean dragging) {

    }

    @Override
    public void setResizable(boolean resizable) {
        // Not supported
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public long getPeer() {
        if (PlatformOS.isWindows) {
            return GLFWNativeWin32.glfwGetWin32Window(this.getHandle());
        } else if (PlatformOS.isMac) {
            return GLFWNativeCocoa.glfwGetCocoaWindow(this.getHandle());
        } else {
            return -1L;
        }
    }

    @Override
    public long getHandle() {
        return window.getWindowHandle();
    }
}
