@file:OptIn(UnsafeApi::class)

package dev.ultreon.quantum.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter
import com.badlogic.gdx.graphics.glutils.HdpiMode
import com.badlogic.gdx.utils.Os
import com.badlogic.gdx.utils.SharedLibraryLoader
import com.esotericsoftware.kryo.kryo5.minlog.Log
import com.github.dgzt.gdx.lwjgl3.Lwjgl3ApplicationConfiguration
import com.github.dgzt.gdx.lwjgl3.Lwjgl3VulkanApplication
import com.github.dgzt.gdx.lwjgl3.Lwjgl3Window
import com.github.dgzt.gdx.lwjgl3.Lwjgl3WindowListener
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef.HWND
import dev.ultreon.quantum.*
import dev.ultreon.quantum.client.Main
import dev.ultreon.quantum.client.QuantumClient
import dev.ultreon.quantum.client.api.events.WindowEvents
import dev.ultreon.quantum.client.input.KeyAndMouseInput
import dev.ultreon.quantum.crash.ApplicationCrash
import dev.ultreon.quantum.crash.CrashLog
import dev.ultreon.quantum.desktop.Dwmapi.Companion.removeBorder
import dev.ultreon.quantum.desktop.Dwmapi.Companion.setAcrylicBackground
import dev.ultreon.quantum.desktop.Dwmapi.Companion.setUseImmersiveDarkMode
import dev.ultreon.quantum.desktop.StatusCode.forAbort
import dev.ultreon.quantum.desktop.StatusCode.forException
import dev.ultreon.quantum.desktop.WindowUtils.makeWindowFrameless
import dev.ultreon.quantum.js.JsLang
import dev.ultreon.quantum.network.system.KyroNetSlf4jLogger
import dev.ultreon.quantum.network.system.KyroSlf4jLogger
import dev.ultreon.quantum.platform.Device
import dev.ultreon.quantum.platform.MouseDevice
import dev.ultreon.quantum.python.PyLang
import org.jetbrains.annotations.ApiStatus
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWNativeWin32
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Taskbar
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.imageio.ImageIO

class DesktopLauncher {
  private class WindowAdapter : Lwjgl3WindowAdapter(), Lwjgl3WindowListener {
    override fun created(window: Lwjgl3Window) {
      gameWindow = DesktopVulkanWindow(window)

      setupMacIcon()
      WindowEvents.WINDOW_CREATED.factory().onWindowCreated(gameWindow)
      setupVibrancy(window.windowHandle)
    }

    override fun created(window: com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window) {
      Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.NOTIFICATION, false)
      Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.LOW, false)
      Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.MEDIUM, true)
      Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.HIGH, true)

      gameWindow = DesktopGLWindow(window)

      setupMacIcon()
      WindowEvents.WINDOW_CREATED.factory().onWindowCreated(gameWindow)
      setupVibrancy(window.windowHandle)
    }

    fun setupMacIcon() {
      if (SharedLibraryLoader.os != Os.MacOsX && Taskbar.isTaskbarSupported()) {
        val taskbar = Taskbar.getTaskbar()

        if (taskbar != null) {
          try {
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
              val res = DesktopLauncher::class.java.getResourceAsStream("/icon.png")
              if (res != null) {
                taskbar.iconImage = ImageIO.read(res)
              } else {
                LOGGER.warn("Failed to extract icon.png")
              }
            }
          } catch (e: Exception) {
            throw RuntimeException(e)
          }

          if (taskbar.isSupported(Taskbar.Feature.ICON_BADGE_TEXT)) {
            taskbar.setIconBadge("?")
          }
        }
      }
    }

    override fun focusLost() {
      val quantumClient = QuantumClient.get() ?: return
      quantumClient.pause()

      WindowEvents.WINDOW_FOCUS_CHANGED.factory().onWindowFocusChanged(quantumClient.window, false)
    }

    override fun focusGained() {
      val quantumClient = QuantumClient.get() ?: return
      WindowEvents.WINDOW_FOCUS_CHANGED.factory().onWindowFocusChanged(quantumClient.window, true)
    }

    override fun closeRequested(): Boolean {
      return !QuantumClient.get().tryShutdown()
    }

    override fun filesDropped(files: Array<String>) {
      val quantumClient = QuantumClient.get() ?: return
      quantumClient.filesDropped(files)

      WindowEvents.WINDOW_FILES_DROPPED.factory().onWindowFilesDropped(quantumClient.window, files)
    }

    companion object {
      var SHA_256: MessageDigest? = null

      init {
        try {
          SHA_256 = MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
          throw RuntimeException(e)
        }
      }

      private fun setupVibrancy(handle: Long) {
        // Check for OS and apply acrylic/mica/vibrancy
        if (GamePlatform.get().isWindows) {
          if (LauncherConfig.get().frameless) {
            makeWindowFrameless(handle)
          }

          val hwnd = HWND(Pointer(GLFWNativeWin32.glfwGetWin32Window(handle)))
          if (LauncherConfig.get().windowVibrancyEnabled) {
            setAcrylicBackground(hwnd)
            setUseImmersiveDarkMode(hwnd, true)
          }

          if (LauncherConfig.get().removeBorder) {
            removeBorder(hwnd)
          }
        }
      }
    }
  }

  companion object {
    val LOGGER: Logger = LoggerFactory.getLogger("Quantum:Launcher")
    lateinit var platform: DesktopPlatform
      private set
    private lateinit var gameWindow: DesktopWindow
    var isWindowVibrancyEnabled: Boolean = false
      private set
    private var fullVibrancyEnabled = false

    /**
     * Launches the game.
     *
     * **Note: This method should not be called.**
     *
     * @param argv the arguments to pass to the game
     */
    @ApiStatus.Internal
    @JvmStatic
    fun main(argv: Array<String>) {
      val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
      Thread.setDefaultUncaughtExceptionHandler(object : Thread.UncaughtExceptionHandler {
        private val logger: Logger = LoggerFactory.getLogger("Quantum:ExceptionHandler")

        override fun uncaughtException(t: Thread, e: Throwable) {
          try {
            if (e is ApplicationCrash) {
              QuantumClient.crash(e.crashLog)
            }

            defaultUncaughtExceptionHandler.uncaughtException(t, e)
          } catch (t1: Throwable) {
            try {
              logger.error("Failed to handle exception", t1)
              Runtime.getRuntime().halt(forException())
            } catch (t2: Throwable) {
              Runtime.getRuntime().halt(forAbort())
            }
          }
        }
      })

      try {
        launch(argv)
      } catch (e: Exception) {
        CommonConstants.LOGGER.error("Failed to launch game", e)
        CrashHandler.handleCrash(CrashLog("Launch failed", e).createCrash().crashLog)
      } catch (e: OutOfMemoryError) {
        CommonConstants.LOGGER.error("Failed to launch game", e)
        CrashHandler.handleCrash(CrashLog("Launch failed", e).createCrash().crashLog)
      }
    }

    /**
     * <h2 style="color: red;">**Note: This method should not be called.**</h2>
     * Launches the game.
     * This method gets invoked dynamically by the FabricMC game provider.
     *
     * @param argv the arguments to pass to the game
     */
    @Suppress("unused")
    private fun launch(argv: Array<String>) {
      if (StartupHelper.startNewJvmIfRequired()) return  // This handles macOS


      val launcherConfig = LauncherConfig.get()
      val useAngleGraphics = launcherConfig.useAngleGraphics && SharedLibraryLoader.os == Os.Windows
      isWindowVibrancyEnabled = launcherConfig.windowVibrancyEnabled
      fullVibrancyEnabled = launcherConfig.enableFullVibrancy

      LauncherConfig.save()

      platform = object : DesktopPlatform(useAngleGraphics) {
        override fun createWindow(): GameWindow {
          return gameWindow
        }

        override fun getMouseDevice(): MouseDevice? {
          return null
        }

        override fun getGameDevices(): Collection<Device> {
          return listOf()
        }

        override fun hasBackPanelRemoved(): Boolean {
          return fullVibrancyEnabled && isWindowVibrancyEnabled && !useAngleGraphics
        }
      }

      Log.setLogger(KyroSlf4jLogger.INSTANCE)
      com.esotericsoftware.minlog.Log.setLogger(KyroNetSlf4jLogger.INSTANCE)

      CrashHandler.addHandler {
        try {
          KeyAndMouseInput.setCursorCaught(false)
          gameWindow.setVisible(false)
        } catch (e: Exception) {
          QuantumClient.LOGGER.error("Failed to hide cursor", e)
        }
      }

      try {
        Files.createDirectories(Paths.get("logs"))
      } catch (e: IOException) {
        throw RuntimeException(e)
      }

      QuantumClient.logDebug()

      PyLang().init()
      JsLang().init()

      GLFW.glfwSetErrorCallback { _: Int, description: Long ->
        QuantumClient.LOGGER.error(
          "GLFW Error: %s",
          description
        )
      }.use {
        try {
          if (GamePlatform.get().isAngleGLES) Lwjgl3VulkanApplication(Main.createInstance(argv), createVulkanConfig())
          else Lwjgl3Application(Main.createInstance(argv), createConfig())
        } catch (e: ApplicationCrash) {
          val crashLog = e.crashLog
          QuantumClient.crash(crashLog)
        } catch (e: Throwable) {
          platform.getLogger("CrashHandler").error("Failed to launch game", e)
          QuantumClient.crash(e)
        }
      }
    }

    private fun createVulkanConfig(): Lwjgl3ApplicationConfiguration {
      val config = Lwjgl3ApplicationConfiguration()
      config.useVsync(false)
      config.setForegroundFPS(0)
      config.setIdleFPS(10)
      config.setBackBufferConfig(4, 4, 4, 4, 8, 4, 0)
      config.setHdpiMode(HdpiMode.Pixels)
      config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES32, 4, 1)
      config.setInitialVisible(false)
      config.setTitle("Quantum Voxel (Vulkan Backend)")
      config.setWindowIcon(*QuantumClient.getIcons())
      config.setWindowedMode(1280, 720)
      config.setWindowListener(WindowAdapter())
      config.setTransparentFramebuffer(GamePlatform.get().hasBackPanelRemoved())

      return config
    }

    private fun createConfig(): com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration {
      val config = com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration()
      config.useVsync(false)
      config.setForegroundFPS(0)
      config.setBackBufferConfig(4, 4, 4, 4, 8, 4, 0)
      config.setHdpiMode(HdpiMode.Pixels)
      config.setOpenGLEmulation(com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation.GL32, 4, 1)
      config.setInitialVisible(false)
      config.setTitle("Quantum Voxel")
      config.setWindowIcon(*QuantumClient.getIcons())
      config.setWindowedMode(1280, 720)
      config.setWindowListener(WindowAdapter())
      config.setTransparentFramebuffer(GamePlatform.get().hasBackPanelRemoved())

      GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4)
      GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1)
      GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)
      GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)

      return config
    }

    fun getGameWindow(): GameWindow {
      return gameWindow
    }
  }
}
