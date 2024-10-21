package dev.ultreon.quantum.desktop;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWNativeWin32;

import java.nio.IntBuffer;

public class WindowUtils {
    private static final User32 user32 = User32.INSTANCE;

    // Constants for window styles
    public static final int GWL_STYLE = -16;
    public static final int WS_BORDER = 0x00800000;
    public static final int WS_CAPTION = 0x00C00000;

    public static void makeWindowFrameless(long glfwWindowHandle) {
        WinDef.HWND hwnd = new WinDef.HWND(new Pointer(GLFWNativeWin32.glfwGetWin32Window(glfwWindowHandle)));

        int currentStyle = user32.GetWindowLong(hwnd, GWL_STYLE);
        int newStyle = currentStyle & ~WS_BORDER & ~WS_CAPTION;

        // Apply the new style
        user32.SetWindowLong(hwnd, GWL_STYLE, newStyle);

        // Redraw window
        user32.SetWindowPos(hwnd, null, 0, 0, 0, 0,
            WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOZORDER | WinUser.SWP_FRAMECHANGED);
    }
}
