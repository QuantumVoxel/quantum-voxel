package dev.ultreon.quantum.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window
import dev.ultreon.mixinprovider.PlatformOS
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWNativeCocoa
import org.lwjgl.glfw.GLFWNativeWin32

class DesktopGLWindow(private val window: Lwjgl3Window) : DesktopWindow() {
  private var title: String? = null
  private val xPos = IntArray(1)
  private val yPos = IntArray(1)

  override fun update() {
    super.update()

    if (isDragging) {
      GLFW.glfwGetWindowPos(handle, xPos, yPos)
    }
  }

  override fun getHandle(): Long = window.windowHandle
  override fun isHovered(): Boolean = true
  override fun isMinimized(): Boolean = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE
  override fun isMaximized(): Boolean =
    !PlatformOS.isMac && GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE

  override fun isFocused(): Boolean = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE
  override fun close() = GLFW.glfwSetWindowShouldClose(handle, true)
  override fun setVisible(visible: Boolean) = if (visible) GLFW.glfwShowWindow(handle) else GLFW.glfwHideWindow(handle)
  override fun minimize() = GLFW.glfwIconifyWindow(handle)
  override fun maximize() = GLFW.glfwMaximizeWindow(handle)

  override fun restore() {
    if (!PlatformOS.isMac) {
      GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_MAXIMIZED, GLFW.GLFW_FALSE)
      GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_ICONIFIED, GLFW.GLFW_FALSE)
    }

    GLFW.glfwRestoreWindow(handle)
  }

  override fun focus() = GLFW.glfwFocusWindow(handle)
  override fun requestAttention() = GLFW.glfwRequestWindowAttention(handle)
  override fun setResizable(resizable: Boolean) =
    GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_RESIZABLE, if (resizable) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)

  override fun getPeer(): Long =
    if (PlatformOS.isWindows) GLFWNativeWin32.glfwGetWin32Window(this.handle)
    else if (PlatformOS.isMac) GLFWNativeCocoa.glfwGetCocoaWindow(this.handle)
    else -1L

  override fun setTitle(title: String) {
    Gdx.graphics.setTitle(title)
    this.title = title
  }

  override fun getTitle(): String = title!!

  override fun setDragging(dragging: Boolean) {

  }
}
