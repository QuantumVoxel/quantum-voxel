package dev.ultreon.quantum.desktop

import com.github.dgzt.gdx.lwjgl3.Lwjgl3Window
import dev.ultreon.mixinprovider.PlatformOS
import org.lwjgl.glfw.GLFWNativeCocoa
import org.lwjgl.glfw.GLFWNativeWin32


class DesktopVulkanWindow(private val window: Lwjgl3Window) : DesktopWindow() {
  private var title: String? = null

  override fun isHovered(): Boolean = window.isFocused
  override fun isFocused(): Boolean = window.isFocused
  override fun isMaximized(): Boolean = false
  override fun isMinimized(): Boolean = window.isIconified
  override fun setVisible(visible: Boolean) = window.setVisible(visible)
  override fun close() = window.flash()
  override fun requestAttention() = window.flash()
  override fun focus() = window.focusWindow()
  override fun minimize() = window.iconifyWindow()
  override fun maximize() = window.maximizeWindow()
  override fun restore() = window.restoreWindow()
  override fun getTitle(): String = title!!
  override fun setTitle(title: String) {
    this.title = title
    window.setTitle(title)
  }

  override fun isDragging(): Boolean = false
  override fun setDragging(dragging: Boolean) = Unit
  override fun setResizable(resizable: Boolean) = Unit // Not supported
  override fun isResizable(): Boolean = true
  override fun getPeer(): Long =
    if (PlatformOS.isWindows) GLFWNativeWin32.glfwGetWin32Window(this.handle)
    else if (PlatformOS.isMac) GLFWNativeCocoa.glfwGetCocoaWindow(this.handle)
    else -1L

  override fun getHandle(): Long = window.windowHandle
}
