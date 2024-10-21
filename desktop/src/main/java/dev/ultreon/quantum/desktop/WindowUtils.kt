package dev.ultreon.quantum.desktop

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinUser
import org.lwjgl.glfw.GLFWNativeWin32

object WindowUtils {
  private val user32: User32 = User32.INSTANCE

  // Constants for window styles
  const val GWL_STYLE: Int = -16
  const val WS_BORDER: Int = 0x00800000
  const val WS_CAPTION: Int = 0x00C00000

  @JvmStatic
  fun makeWindowFrameless(glfwWindowHandle: Long) {
    val hwnd = HWND(Pointer(GLFWNativeWin32.glfwGetWin32Window(glfwWindowHandle)))

    val currentStyle = user32.GetWindowLong(hwnd, GWL_STYLE)
    val newStyle = currentStyle and WS_BORDER.inv() and WS_CAPTION.inv()

    // Apply the new style
    user32.SetWindowLong(hwnd, GWL_STYLE, newStyle)

    // Redraw window
    user32.SetWindowPos(
      hwnd, null, 0, 0, 0, 0,
      WinUser.SWP_NOMOVE or WinUser.SWP_NOSIZE or WinUser.SWP_NOZORDER or WinUser.SWP_FRAMECHANGED
    )
  }
}
