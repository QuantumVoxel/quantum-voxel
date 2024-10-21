package dev.ultreon.quantum.desktop.imgui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g3d.model.Node
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import dev.ultreon.quantum.GamePlatform
import dev.ultreon.quantum.client.QuantumClient
import dev.ultreon.quantum.client.shaders.WorldShader
import dev.ultreon.quantum.client.util.deg
import dev.ultreon.quantum.client.world.ClientWorld
import dev.ultreon.quantum.client.world.ClientWorldAccess
import dev.ultreon.quantum.client.world.Skybox
import dev.ultreon.quantum.desktop.DesktopLauncher
import dev.ultreon.quantum.entity.EntityType
import dev.ultreon.quantum.registry.Registries
import dev.ultreon.quantum.server.QuantumServer
import dev.ultreon.quantum.util.NamespaceID
import dev.ultreon.quantum.util.RgbColor
import dev.ultreon.quantum.util.Vec2f
import dev.ultreon.quantum.util.Vec3f
import dev.ultreon.quantum.world.vec.ChunkVec
import dev.ultreon.quantum.world.vec.ChunkVecSpace
import imgui.ImGui
import imgui.extension.imguifiledialog.ImGuiFileDialog
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags
import imgui.extension.implot.ImPlot
import imgui.extension.implot.ImPlotContext
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiInputTextFlags
import imgui.flag.ImGuiWindowFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import imgui.type.ImBoolean
import imgui.type.ImFloat
import imgui.type.ImInt
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import java.lang.String.CASE_INSENSITIVE_ORDER
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture

object ImGuiOverlay {
  val I_GAMMA: ImFloat = ImFloat(1.5f)
  val U_CAP: ImFloat = ImFloat(0.45f)
  val U_RADIUS: ImFloat = ImFloat(0.45f)
  val U_INTENSITY: ImFloat = ImFloat(1.5f)
  val U_MULTIPLIER: ImFloat = ImFloat(1000.0f)
  val U_DEPTH_TOLERANCE: ImFloat = ImFloat(0.0001f)
  val U_ATLAS_SIZE: ImInt = ImInt(512)
  val MODEL_VIEWER_LIST_INDEX: ImInt = ImInt(0)
  @JvmField
  val SHOW_RENDER_PIPELINE: ImBoolean = ImBoolean(false)
  private val SHOW_IM_GUI = ImBoolean(false)
  private val SHOW_PLAYER_UTILS = ImBoolean(false)
  private val SHOW_GUI_UTILS = ImBoolean(false)
  private val SHOW_UTILS = ImBoolean(false)
  private val SHOW_SHADER_EDITOR = ImBoolean(false)
  private val SHOW_SKYBOX_EDITOR = ImBoolean(false)
  private val SHOW_MODEL_VIEWER = ImBoolean(false)
  private val SHOW_CHUNK_SECTION_BORDERS = ImBoolean(false)
  private val SHOW_CHUNK_DEBUGGER = ImBoolean(false)
  private val SHOW_PROFILER = ImBoolean(false)

  private val RESET_CHUNK = ChunkVec(17, 4, 18, ChunkVecSpace.WORLD)
  internal val keys: Array<String> = arrayOf("A", "B", "C")
  internal val values: Array<Double> = arrayOf(0.1, 0.3, 0.6)
  private val TRANSLATE_TMP = Vector3()
  private val SCALE_TMP = Vector3()
  private val ROTATE_TMP = Quaternion()

  private var imGuiGlfw: ImGuiImplGlfw? = null
  private var imGuiGl3: ImGuiImplGl3? = null
  private var isImplCreated = false
  private var isContextCreated = false
  private val guiEditor = GuiEditor()
  private var triggerLoadWorld = false
  private var imPlotCtx: ImPlotContext? = null
  private var modelViewerList = arrayOfNulls<String>(0)

  @JvmStatic
  fun setupImGui() {
    if (GamePlatform.get().isAngleGLES) return

    QuantumClient.LOGGER.info("Setting up ImGui")

    QuantumClient.get().deferClose(GLFWErrorCallback.create { _: Int, description: Long ->
      QuantumClient.LOGGER.error(
        "GLFW Error: %s",
        description
      )
    }.set())
    check(GLFW.glfwInit()) { "Unable to initialize GLFW" }
    synchronized(ImGuiOverlay::class.java) {
      ImGui.createContext()
      imPlotCtx = ImPlot.createContext()
      isContextCreated = true
    }
    val io = ImGui.getIO()
    io.iniFilename = null
    io.fonts.addFontDefault()

    val windowHandle = DesktopLauncher.getGameWindow().handle

    QuantumClient.invokeAndWait {
      imGuiGlfw!!.init(windowHandle, true)
      imGuiGl3!!.init("#version 140")
    }
  }

  @JvmStatic
  fun preInitImGui() {
    if (GamePlatform.get().isAngleGLES) return

    synchronized(ImGuiOverlay::class.java) {
      imGuiGlfw = ImGuiImplGlfw()
      imGuiGl3 = ImGuiImplGl3()
      isImplCreated = true
    }
  }

  @JvmStatic
  val isChunkSectionBordersShown: Boolean
    get() = SHOW_CHUNK_SECTION_BORDERS.get()

  @JvmStatic
  fun renderImGui(client: QuantumClient) {
    if (!SHOW_IM_GUI.get()) return
    if (GamePlatform.get().isAngleGLES) return

    imGuiGlfw!!.newFrame()

    ImGui.newFrame()
    ImGui.setNextWindowPos(0f, 0f)
    ImGui.setNextWindowSize(client.width.toFloat(), 18f)
    ImGui.setNextWindowCollapsed(true)

    if (Gdx.input.isCursorCatched) {
      ImGui.getIO().setMouseDown(BooleanArray(5))
      ImGui.getIO().setMousePos(
        Int.MAX_VALUE.toFloat(),
        Int.MAX_VALUE.toFloat()
      )
    }

    renderDisplay()

    if (ImGui.begin(
        "MenuBar", ImGuiWindowFlags.NoMove or
                ImGuiWindowFlags.NoCollapse or
                ImGuiWindowFlags.AlwaysAutoResize or
                ImGuiWindowFlags.NoTitleBar or
                ImGuiWindowFlags.MenuBar or
                ImGuiInputTextFlags.AllowTabInput
      )
    ) {
      renderMenuBar()
      ImGui.end()
    }

    renderWindows(client)

    handleTriggers()

    ImGui.render()
    imGuiGl3!!.renderDrawData(ImGui.getDrawData())

    handleInput()
  }

  private fun renderDisplay() {
    if (ImGuiFileDialog.display("Main::loadWorld", ImGuiFileDialogFlags.None, 200f, 400f, 800f, 600f)) {
      if (ImGuiFileDialog.isOk()) {
        val filePathName = Path.of(ImGuiFileDialog.getFilePathName())
        QuantumClient.invoke {
          QuantumClient.get().startWorld(filePathName)
        }
      }
      ImGuiFileDialog.close()
    }
  }

  private fun handleTriggers() {
    if (triggerLoadWorld) {
      triggerLoadWorld = false
      ImGuiFileDialog.openModal(
        "Main::loadWorld",
        "Choose Folder",
        null,
        QuantumClient.getGameDir().toAbsolutePath().toString(),
        "",
        1,
        7,
        ImGuiFileDialogFlags.None
      )
    }
  }

  private fun renderWindows(client: QuantumClient) {
    if (SHOW_PLAYER_UTILS.get()) showPlayerUtilsWindow(client)
    if (SHOW_GUI_UTILS.get()) showGuiEditor(client)
    if (SHOW_UTILS.get()) showUtils(client)
    if (SHOW_CHUNK_DEBUGGER.get()) showChunkDebugger(client)
    if (SHOW_SHADER_EDITOR.get()) showShaderEditor()
    if (SHOW_SKYBOX_EDITOR.get()) showSkyboxEditor()
    if (SHOW_MODEL_VIEWER.get()) showModelViewer()
  }

  private fun showModelViewer() {
    ImGui.setNextWindowSize(400f, 200f, ImGuiCond.Once)
    ImGui.setNextWindowPos(ImGui.getMainViewport().posX + 100, ImGui.getMainViewport().posY + 100, ImGuiCond.Once)
    if (ImGui.begin("Model Viewer", defaultFlags)) {
      if (ImGui.button("Reload")) {
        modelViewerList = QuantumClient.get().entityModelManager.registry.keys.asSequence()
          .map { obj: EntityType<*> -> obj.id }.map { o: NamespaceID? -> Objects.toString(o) }
          .sortedWith(CASE_INSENSITIVE_ORDER).toList().toTypedArray()
      }

      ImGui.text("Select Model:")
      ImGui.sameLine()
      ImGui.listBox("##ModelViewer::ListBox", MODEL_VIEWER_LIST_INDEX, modelViewerList)

      if (modelViewerList.isEmpty()) {
        ImGui.text("No models found")
      } else {
        val s = modelViewerList[MODEL_VIEWER_LIST_INDEX.get()]
        val id = NamespaceID(s!!)
        val entityType = Registries.ENTITY_TYPE[id]
        if (entityType != null) {
          val model = QuantumClient.get().entityModelManager.getFinished(entityType)
          if (model != null) {
            if (ImGui.treeNode("Model")) {
              ImGui.text("Model Name:")
              ImGui.sameLine()
              ImGui.text(s)

              if (ImGui.treeNode("Nodes")) {
                for (node in model.nodes.toArray()) {
                  drawNode(node)
                }

                ImGui.treePop()
              }

              ImGui.treePop()
            }
          }
        }
      }

      if (ImGui.button("Close")) {
        SHOW_MODEL_VIEWER.set(false)
      }
    }
    ImGui.end()
  }

  private fun drawNode(node: Node) {
    if (ImGui.treeNode(node.id)) {
      ImGui.text("Name:")
      ImGui.sameLine()
      ImGui.text(node.id)

      ImGui.text("Local Transform:")
      ImGui.treePush()
      drawTransform(node.localTransform, node)
      ImGui.treePop()

      ImGui.text("Global Transform:")
      ImGui.treePush()
      drawTransform(node.globalTransform, node)
      ImGui.treePop()

      for (child in node.children) {
        drawNode(child)
      }

      ImGui.treePop()
    }
  }

  private fun drawTransform(node: Matrix4, node1: Node) {
    val translation = node.getTranslation(TRANSLATE_TMP)
    drawVec3("Translation:", translation)

    val scale = node1.localTransform.getScale(SCALE_TMP)
    drawVec3("Scale:", scale)

    val rotation = node1.localTransform.getRotation(ROTATE_TMP)
    ImGui.text("Rotation:")
    ImGui.sameLine()
    ImGui.text("X: " + rotation.x + " Y: " + rotation.y + " Z: " + rotation.z + " W: " + rotation.w)
  }

  private fun drawVec3(name: String, vec3: Vector3) {
    ImGui.text(name)
    ImGui.sameLine()
    ImGui.text("X: " + vec3.x + " Y: " + vec3.y + " Z: " + vec3.z)
  }

  private fun handleInput() {
    if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) return

    if (Gdx.input.isKeyJustPressed(Input.Keys.O)) triggerLoadWorld = true
    else if (Gdx.input.isKeyJustPressed(Input.Keys.P)) SHOW_PLAYER_UTILS.set(!SHOW_PLAYER_UTILS.get())
    else if (Gdx.input.isKeyJustPressed(Input.Keys.G)) SHOW_GUI_UTILS.set(!SHOW_GUI_UTILS.get())
    else if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) SHOW_CHUNK_SECTION_BORDERS.set(!SHOW_CHUNK_SECTION_BORDERS.get())
  }

  private fun renderMenuBar() {
    if (ImGui.beginMenuBar()) {
      if (ImGui.beginMenu("File")) {
        if (ImGui.menuItem("Load World...", "Ctrl+O")) {
          triggerLoadWorld = true
        }
        ImGui.endMenu()
      }
      if (ImGui.beginMenu("Edit")) {
        ImGui.menuItem("Player Editor", "Ctrl+P", SHOW_PLAYER_UTILS)
        ImGui.menuItem("Gui Editor", "Ctrl+G", SHOW_GUI_UTILS)
        ImGui.menuItem("Shader Editor", "", SHOW_SHADER_EDITOR)
        ImGui.menuItem("Skybox Editor (Deprecated)", "", SHOW_SKYBOX_EDITOR)
        ImGui.endMenu()
      }
      if (ImGui.beginMenu("View")) {
        ImGui.menuItem("Utils", null, SHOW_UTILS)
        ImGui.menuItem("Chunks", null, SHOW_CHUNK_DEBUGGER)
        ImGui.menuItem("Chunk Node Borders", "Ctrl+F4", SHOW_CHUNK_SECTION_BORDERS)
        ImGui.menuItem("InspectionRoot", "Ctrl+P", SHOW_PROFILER)
        ImGui.menuItem("Render Pipeline", null, SHOW_RENDER_PIPELINE)
        ImGui.menuItem("Model Viewer", null, SHOW_MODEL_VIEWER)
        ImGui.endMenu()
      }
      if (ImGui.beginMenu("Gizmos")) {
        val terrainRenderer: ClientWorldAccess? = QuantumClient.get().world
        if (terrainRenderer is ClientWorld) {
          for (category in terrainRenderer.gizmoCategories) {
            if (ImGui.menuItem("Gizmo '$category'", null, terrainRenderer.isGimzoCategoryEnabled(category))) {
              terrainRenderer.toggleGizmoCategory(category)
            }
          }
        }
        ImGui.endMenu()
      }
      if (ImGui.beginMenu("Resources")) {
        if (ImGui.menuItem("Reload Resources", "F1+R")) {
          QuantumClient.get().reloadResourcesAsync()
        }
        ImGui.endMenu()
      }

      ImGui.text(" FPS: " + Gdx.graphics.framesPerSecond + " ")
      ImGui.sameLine()
      ImGui.text(" Client TPS: " + Gdx.graphics.framesPerSecond + " ")
      ImGui.sameLine()
      val server = QuantumServer.get()
      if (server != null) {
        ImGui.text(" Server TPS: " + server.currentTps + " ")
        ImGui.sameLine()
      }
      ImGui.text(" Frame ID: " + Gdx.graphics.frameId + " ")
      ImGui.endMenuBar()
    }
  }

  private fun showChunkDebugger(client: QuantumClient) {
    ImGui.setNextWindowSize(400f, 200f, ImGuiCond.Once)
    ImGui.setNextWindowPos(ImGui.getMainViewport().posX + 100, ImGui.getMainViewport().posY + 100, ImGuiCond.Once)
    if (client.player != null && ImGui.begin("Chunk Debugging", defaultFlags)) {
      if (ImGui.button(String.format("Reset chunk at %s", RESET_CHUNK))) {
        CompletableFuture.runAsync {
          val world: ClientWorldAccess? = client.world
          QuantumClient.invokeAndWait {
            world?.unloadChunk(RESET_CHUNK)
          }
          QuantumServer.invokeAndWait {
            client.integratedServer.overworld.regenerateChunk(
              RESET_CHUNK
            )
          }
        }
      }
      ImGui.end()
    }
  }

  private fun showShaderEditor() {
    ImGui.setNextWindowSize(400f, 200f, ImGuiCond.Once)
    ImGui.setNextWindowPos(ImGui.getMainViewport().posX + 100, ImGui.getMainViewport().posY + 100, ImGuiCond.Once)
    if (ImGui.begin("Shader Editor", defaultFlags)) {
      if (ImGui.treeNode("Shader::SSAO", "SSAO")) {
        ImGuiEx.editFloat("iGamma", "Shader::SSAO::iGamma",
          { I_GAMMA.get() },
          { value: Float? ->
            I_GAMMA.set(
              value!!
            )
          })
        ImGui.treePop()
      }

      if (ImGui.treeNode("Shader::SkyBox", "SkyBox")) {
        ImGuiEx.editColor3Gdx(
          "DayTopColor", "Shader::SkyBox::DayTopColor"
        ) { ClientWorld.DAY_TOP_COLOR }
        ImGuiEx.editColor3Gdx(
          "DayBottomColor", "Shader::SkyBox::DayBottomColor"
        ) { ClientWorld.DAY_BOTTOM_COLOR }
        ImGuiEx.editColor3Gdx(
          "NightTopColor", "Shader::SkyBox::NightTopColor"
        ) { ClientWorld.NIGHT_TOP_COLOR }
        ImGuiEx.editColor3Gdx(
          "NightBottomColor", "Shader::SkyBox::NightBottomColor"
        ) { ClientWorld.NIGHT_BOTTOM_COLOR }
        ImGuiEx.editColor3Gdx(
          "SunRiseSetColor", "Shader::SkyBox::SunRiseSetColor"
        ) { ClientWorld.SUN_RISE_COLOR }
        ImGuiEx.editBool("Debug", "Shader::SkyBox::Debug",
          { Skybox.debug },
          { Skybox.debug = it })
        ImGuiEx.editFloat(
          "Rotation", "Shader::SkyBox::Rotation", ClientWorld.SKYBOX_ROTATION::degrees
        ) { setSkyboxRot(it) }
        ImGui.treePop()
      }

      if (ImGui.treeNode("Shader::World", "World")) {
        ImGuiEx.editColor3("FogColor", "Shader::World::FogColor",
          { ClientWorld.FOG_COLOR.get() },
          { newValue: RgbColor? -> ClientWorld.FOG_COLOR.set(newValue) })
        ImGuiEx.editDouble("FogDensity", "Shader::World::FogDensity",
          { ClientWorld.FOG_DENSITY.get() },
          { newValue: Double -> ClientWorld.FOG_DENSITY.set(newValue) })
        ImGuiEx.editDouble("FogStart", "Shader::World::FogStart",
          { ClientWorld.FOG_START.get() },
          { newValue: Double -> ClientWorld.FOG_START.set(newValue) })
        ImGuiEx.editDouble("FogEnd", "Shader::World::FogEnd",
          { ClientWorld.FOG_END.get() },
          { newValue: Double -> ClientWorld.FOG_END.set(newValue) })
        ImGuiEx.editVec2f("AtlasSize", "Shader::World::AtlasSize",
          { ClientWorld.ATLAS_SIZE.get() },
          { newValue: Vec2f? -> ClientWorld.ATLAS_SIZE.set(newValue) })
        ImGuiEx.editVec3f("CameraUp", "Shader::World::CameraUp",
          { Vec3f(WorldShader.CAMERA_UP.x, WorldShader.CAMERA_UP.y, WorldShader.CAMERA_UP.z) },
          { WorldShader.CAMERA_UP[it!!.x, it.y] = it.z })
        ImGui.treePop()
      }

      ImGui.end()
    }
  }

  private fun showSkyboxEditor() {
    ImGui.setNextWindowSize(400f, 200f, ImGuiCond.Once)
    ImGui.setNextWindowPos(ImGui.getMainViewport().posX + 100, ImGui.getMainViewport().posY + 100, ImGuiCond.Once)
    if (ImGui.begin("Skybox Editor (Deprecated)", defaultFlags)) {
      ImGuiEx.editColor3Gdx(
        "DayTopColor", "Shader::SkyBox::DayTopColor"
      ) { ClientWorld.DAY_TOP_COLOR }
      ImGuiEx.editColor3Gdx(
        "DayBottomColor", "Shader::SkyBox::DayBottomColor"
      ) { ClientWorld.DAY_BOTTOM_COLOR }
      ImGuiEx.editColor3Gdx(
        "NightTopColor", "Shader::SkyBox::NightTopColor"
      ) { ClientWorld.NIGHT_TOP_COLOR }
      ImGuiEx.editColor3Gdx(
        "NightBottomColor", "Shader::SkyBox::NightBottomColor"
      ) { ClientWorld.NIGHT_BOTTOM_COLOR }
      ImGuiEx.editColor3Gdx(
        "SunRiseSetColor", "Shader::SkyBox::SunRiseSetColor"
      ) { ClientWorld.SUN_RISE_COLOR }
      ImGuiEx.editBool("Debug", "Shader::SkyBox::Debug",
        { Skybox.debug },
        { Skybox.debug = it })
      ImGuiEx.editFloat(
        "Rotation", "Shader::SkyBox::Rotation", ClientWorld.SKYBOX_ROTATION::degrees
      ) { setSkyboxRot(it) }
      ImGui.end()
    }
  }

  private fun showPlayerUtilsWindow(client: QuantumClient) {
    ImGui.setNextWindowSize(400f, 200f, ImGuiCond.Once)
    ImGui.setNextWindowPos(ImGui.getMainViewport().posX + 100, ImGui.getMainViewport().posY + 100, ImGuiCond.Once)
    if (client.player != null && ImGui.begin("Player Utils", defaultFlags)) {
      ImGuiEx.text("Id:") { client.player!!.id }
      ImGuiEx.text("Dead:") { client.player!!.isDead }
      ImGuiEx.editFloat("Walking Speed:", "PlayerWalkingSpeed",
        { client.player!!.walkingSpeed },
        { walkingSpeed: Float? ->
          client.player!!.walkingSpeed =
            walkingSpeed!!
        })
      ImGuiEx.editFloat("Flying Speed:", "PlayerFlyingSpeed",
        { client.player!!.flyingSpeed },
        { flyingSpeed: Float? ->
          client.player!!.flyingSpeed =
            flyingSpeed!!
        })
      ImGuiEx.editFloat("Gravity:", "PlayerGravity",
        { client.player!!.gravity },
        { v: Float -> client.player!!.gravity = v })
      ImGuiEx.editFloat("Jump Velocity:", "PlayerJumpVelocity",
        { client.player!!.jumpVel },
        { v: Float -> client.player!!.jumpVel = v })
      ImGuiEx.editFloat("Health:", "PlayerHealth",
        { client.player!!.health },
        { health: Float? ->
          client.player!!.health =
            health!!
        })
      ImGuiEx.editFloat("Max Health:", "PlayerMaxHealth",
        { client.player!!.maxHealth },
        { maxHealth: Float? ->
          client.player!!.maxHealth =
            maxHealth!!
        })
      ImGuiEx.editBool("No Gravity:", "PlayerNoGravity",
        { client.player!!.noGravity },
        { v: Boolean -> client.player!!.noGravity = v })
      ImGuiEx.editBool("Flying:", "PlayerFlying",
        { client.player!!.isFlying },
        { flying: Boolean? ->
          client.player!!.isFlying =
            flying!!
        })
      ImGuiEx.editBool("Allow Flight:", "PlayerAllowFlight",
        { client.player!!.isAllowFlight },
        { })
      ImGuiEx.bool("On Ground:") { client.player!!.onGround }
      ImGuiEx.bool("Colliding:") { client.player!!.isColliding }
      ImGuiEx.bool("Colliding X:") { client.player!!.isCollidingX }
      ImGuiEx.bool("Colliding Y:") { client.player!!.isCollidingY }
      ImGuiEx.bool("Colliding Z:") { client.player!!.isCollidingZ }

      if (ImGui.collapsingHeader("Position")) {
        ImGui.treePush()
        ImGuiEx.editDouble("X:", "PlayerX",
          { client.player!!.x },
          { v: Double -> client.player!!.x = v })
        ImGuiEx.editDouble("Y:", "PlayerY",
          { client.player!!.y },
          { v: Double -> client.player!!.y = v })
        ImGuiEx.editDouble("Z:", "PlayerZ",
          { client.player!!.z },
          { v: Double -> client.player!!.z = v })
        ImGui.treePop()
      }
      if (ImGui.collapsingHeader("Velocity")) {
        ImGui.treePush()
        ImGuiEx.editDouble("X:", "PlayerVelocityX",
          { client.player!!.velocityX },
          { v: Double -> client.player!!.velocityX = v })
        ImGuiEx.editDouble("Y:", "PlayerVelocityY",
          { client.player!!.velocityY },
          { v: Double -> client.player!!.velocityY = v })
        ImGuiEx.editDouble("Z:", "PlayerVelocityZ",
          { client.player!!.velocityZ },
          { v: Double -> client.player!!.velocityZ = v })
        ImGui.treePop()
      }
      if (ImGui.collapsingHeader("Rotation")) {
        ImGui.treePush()
        ImGuiEx.editFloat("X:", "PlayerXRot",
          { client.player!!.getXRot() },
          { v: Float -> client.player!!.setXRot(v) })
        ImGuiEx.editFloat("Y:", "PlayerYRot",
          { client.player!!.getYRot() },
          { v: Float -> client.player!!.setYRot(v) })
        ImGui.treePop()
      }
      if (ImGui.collapsingHeader("Player Input")) {
        ImGui.treePush()
        ImGuiEx.bool("Forward") { client.playerInput.forward }
        ImGuiEx.bool("Backward") { client.playerInput.backward }
        ImGuiEx.bool("Left") { client.playerInput.strafeLeft }
        ImGuiEx.bool("Right") { client.playerInput.strafeRight }
        ImGuiEx.bool("Up") { client.playerInput.up }
        ImGuiEx.bool("Down") { client.playerInput.down }
        ImGui.treePop()
      }
      ImGui.end()
    }
  }

  private fun showGuiEditor(client: QuantumClient) {
    ImGui.setNextWindowSize(400f, 200f, ImGuiCond.Once)
    ImGui.setNextWindowPos(ImGui.getMainViewport().posX + 100, ImGui.getMainViewport().posY + 100, ImGuiCond.Once)
    if (ImGui.begin("GUI Editor", defaultFlags)) {
      guiEditor.render(client)
    }
    ImGui.end()
  }

  private fun showUtils(client: QuantumClient) {
    ImGui.setNextWindowSize(400f, 200f, ImGuiCond.Once)
    ImGui.setNextWindowPos(ImGui.getMainViewport().posX + 100, ImGui.getMainViewport().posY + 100, ImGuiCond.Once)
    if (ImGui.begin("Utils", defaultFlags)) {
      ImGuiEx.slider(
        "FOV", "GameFOV", client.camera.fov.toInt(), 10, 150
      ) { i: Int -> client.camera.fov = i.toFloat() }
    }
    ImGui.end()
  }

  private val defaultFlags: Int
    get() {
      val cursorCaught = Gdx.input.isCursorCatched
      var flags = ImGuiWindowFlags.None
      if (cursorCaught) flags =
        flags or (ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoInputs)
      return flags
    }

  @JvmStatic
  val isShown: Boolean
    get() = SHOW_IM_GUI.get()

  @JvmStatic
  fun setShowingImGui(value: Boolean) {
    SHOW_IM_GUI.set(value)
  }

  val isProfilerShown: Boolean
    get() = SHOW_PROFILER.get()

  @JvmStatic
  fun dispose() {
    if (GamePlatform.get().isAngleGLES) return

    synchronized(ImGuiOverlay::class.java) {
      if (isImplCreated) {
        imGuiGl3!!.dispose()
        imGuiGlfw!!.dispose()
      }
      if (isContextCreated) {
        ImGui.destroyContext()
        ImPlot.destroyContext(imPlotCtx)
      }
    }
  }

  private fun setSkyboxRot(v: Float) {
    ClientWorld.SKYBOX_ROTATION = v.deg()
  }
}
