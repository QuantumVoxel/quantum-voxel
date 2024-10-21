package dev.ultreon.quantum.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import com.badlogic.gdx.utils.GdxRuntimeException
import dev.ultreon.quantum.*
import dev.ultreon.quantum.client.QuantumClient
import dev.ultreon.quantum.desktop.imgui.ImGuiOverlay
import dev.ultreon.quantum.desktop.imgui.ImGuiOverlay.dispose
import dev.ultreon.quantum.desktop.imgui.ImGuiOverlay.isChunkSectionBordersShown
import dev.ultreon.quantum.desktop.imgui.ImGuiOverlay.isShown
import dev.ultreon.quantum.desktop.imgui.ImGuiOverlay.renderImGui
import dev.ultreon.quantum.js.JsLoader
import dev.ultreon.quantum.log.Logger
import dev.ultreon.quantum.python.PyLoader
import dev.ultreon.quantum.util.Env
import dev.ultreon.quantum.util.Result
import dev.ultreon.xeox.loader.XeoxLoader
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModOrigin
import org.lwjgl.system.Configuration
import org.lwjgl.system.Platform
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.lang.management.ManagementFactory
import java.net.URI
import java.nio.file.Path
import java.util.*
import java.util.function.Consumer
import javax.swing.JFileChooser

abstract class DesktopPlatform internal constructor(private val angleGLES: Boolean) : GamePlatform() {
  private val mods: MutableMap<String, FabricMod> = IdentityHashMap()

  init {
    if (angleGLES) System.setProperty("quantum.platform.anglegles", "true")
  }

  override fun preInitImGui() {
    ImGuiOverlay.preInitImGui()
  }

  override fun setupImGui() {
    ImGuiOverlay.setupImGui()
  }

  override fun renderImGui() {
    renderImGui(QuantumClient.get())
  }

  override fun onFirstRender() {
    val graphics = Gdx.graphics as Lwjgl3Graphics
    val window = graphics.window
    window.setVisible(true)
  }

  override fun onGameDispose() {
    dispose()
  }

  override fun isShowingImGui(): Boolean {
    return isShown
  }

  override fun setShowingImGui(value: Boolean) {
    ImGuiOverlay.setShowingImGui(value)
  }

  override fun areChunkBordersVisible(): Boolean {
    return isChunkSectionBordersShown
  }

  override fun showRenderPipeline(): Boolean {
    return ImGuiOverlay.SHOW_RENDER_PIPELINE.get()
  }

  override fun getMod(id: String): Optional<Mod> {
    return FabricLoader.getInstance()
      .getModContainer(id)
      .map { container -> mods.computeIfAbsent(id) { FabricMod(container) } as Mod }
      .or { super.getMod(id) }
  }

  override fun isModLoaded(id: String): Boolean {
    return FabricLoader.getInstance().isModLoaded(id) || super.isModLoaded(id)
  }

  override fun getMods(): Collection<Mod> {
    val list = ArrayList<Mod>()
    list.addAll(
      FabricLoader.getInstance().allMods.stream().map { container ->
        mods.computeIfAbsent(container.metadata.id) { FabricMod(container) }
      }.toList()
    )
    list.addAll(JsLoader.getInstance().mods)
    list.addAll(PyLoader.getInstance().mods)
    list.addAll(super.getMods())
    return list
  }

  override fun isDevEnvironment(): Boolean {
    return FabricLoader.getInstance().isDevelopmentEnvironment
  }

  override fun <T> invokeEntrypoint(name: String, initClass: Class<T>, init: Consumer<T>) {
    FabricLoader.getInstance().invokeEntrypoints(name, initClass, init)
  }

  override fun getEnv(): Env {
    return when (FabricLoader.getInstance().environmentType) {
      EnvType.CLIENT -> Env.CLIENT
      EnvType.SERVER -> Env.SERVER
      null -> throw GdxRuntimeException("Unknown environment type!")
    }
  }

  override fun getConfigDir(): Path {
    return FabricLoader.getInstance().configDir
  }

  override fun getGameDir(): Path {
    return FabricLoader.getInstance().gameDir
  }

  override fun openImportDialog(): Result<Boolean> {
    val jFileChooser = JFileChooser()
    jFileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
    jFileChooser.isMultiSelectionEnabled = true
    val result = jFileChooser.showOpenDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
      val selectedFiles = jFileChooser.selectedFiles
      for (file in selectedFiles) {
        return XeoxLoader.get().importMod(file).map(
          { true },
          { v: Throwable? -> v })
      }
      return Result.ok(false)
    }
    return Result.ok(false)
  }

  override fun isDesktop(): Boolean {
    return true
  }

  override fun locateResources() {
    try {
      val resource = QuantumClient::class.java.getResource("/.quantum-resources")
        ?: throw GdxRuntimeException("Quantum Voxel resources unavailable!")
      var path = resource.toString()

      if (path.startsWith("jar:")) {
        path = path.substring("jar:".length)
      }

      path = path.substring(0, path.lastIndexOf('/'))

      if (path.endsWith("!")) {
        path = path.substring(0, path.length - 1)
      }

      QuantumClient.get().resourceManager.importPackage(File(URI(path)).toPath())
    } catch (e: Exception) {
      for (rootPath in FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow().rootPaths) {
        try {
          QuantumClient.get().resourceManager.importPackage(rootPath)
        } catch (ex: IOException) {
          QuantumClient.crash(ex)
        }
      }
    }
  }

  override fun locateModResources() {
    for (mod in FabricLoader.getInstance().allMods) {
      if (mod.origin.kind != ModOrigin.Kind.PATH) continue

      for (rootPath in mod.rootPaths) {
        // Try to import a resource package for the given mod path.
        try {
          QuantumClient.get().resourceManager.importPackage(rootPath)
        } catch (e: IOException) {
          CommonConstants.LOGGER.warn("Importing resources failed for path: " + rootPath.toFile(), e)
        }
      }
    }
  }

  override fun isMacOSX(): Boolean {
    return Platform.get() === Platform.MACOSX
  }

  override fun isWindows(): Boolean {
    return Platform.get() === Platform.WINDOWS
  }

  override fun isLinux(): Boolean {
    return Platform.get() === Platform.LINUX
  }

  @Deprecated("")
  override fun setupMacOSX() {
    if (isMacOSX) {
      Configuration.GLFW_LIBRARY_NAME.set("glfw_async")
      Configuration.GLFW_CHECK_THREAD0.set(false)
    }
  }

  override fun launch(argv: Array<String>) {
  }

  override fun close() {
    (Gdx.graphics as Lwjgl3Graphics).window.closeWindow()
  }

  override fun setVisible(visible: Boolean) {
    DesktopLauncher.getGameWindow().setVisible(visible)
  }

  override fun requestAttention() {
    (Gdx.graphics as Lwjgl3Graphics).window.flash()
  }

  override fun getLogger(name: String): Logger {
    return Slf4jLogger(LoggerFactory.getLogger(name))
  }

  override fun detectDebug(): Boolean {
    val args = ManagementFactory.getRuntimeMXBean().inputArguments
    val debugFlagPresent = args.contains("-Xdebug")
    val jdwpPresent = args.toString().contains("jdwp")
    return debugFlagPresent || jdwpPresent
  }

  abstract override fun createWindow(): GameWindow

  override fun isMouseCaptured(): Boolean {
    return Gdx.input.isCursorCatched
  }

  override fun setMouseCaptured(captured: Boolean) {
    Gdx.input.isCursorCatched = captured
  }

  override fun setCursorPosition(x: Int, y: Int) {
    Gdx.input.setCursorPosition(x, y)
  }

  override fun getDeviceType(): DeviceType {
    return DeviceType.DESKTOP
  }

  override fun setTransparentFBO(enable: Boolean) {
//        GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, enable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
  }

  override fun isAngleGLES(): Boolean {
    return angleGLES
  }

  override fun isGLES(): Boolean {
    return angleGLES || isMacOSX
  }

  override fun hasBackPanelRemoved(): Boolean {
    return false
  }
}
