package dev.ultreon.quantum.desktop.imgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.mixinprovider.PlatformOS;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.shaders.WorldShader;
import dev.ultreon.quantum.client.util.Rot;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.client.world.Skybox;
import dev.ultreon.quantum.desktop.DesktopLauncher;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Vec3f;
import dev.ultreon.quantum.world.vec.ChunkVec;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ImGuiOverlay {
    public static final ImFloat I_GAMMA = new ImFloat(1.5f);
    public static final ImFloat U_CAP = new ImFloat(0.45f);
    public static final ImFloat U_RADIUS = new ImFloat(0.45f);
    public static final ImFloat U_INTENSITY = new ImFloat(1.5f);
    public static final ImFloat U_MULTIPLIER = new ImFloat(1000.0f);
    public static final ImFloat U_DEPTH_TOLERANCE = new ImFloat(0.0001f);
    public static final ImInt U_ATLAS_SIZE = new ImInt(512);
    public static final ImInt MODEL_VIEWER_LIST_INDEX = new ImInt(0);
    public static final ImBoolean SHOW_RENDER_PIPELINE = new ImBoolean(false);
    private static final ImBoolean SHOW_IM_GUI = new ImBoolean(false);
    private static final ImBoolean SHOW_PLAYER_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_GUI_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_SHADER_EDITOR = new ImBoolean(false);
    private static final ImBoolean SHOW_SKYBOX_EDITOR = new ImBoolean(false);
    private static final ImBoolean SHOW_MODEL_VIEWER = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_SECTION_BORDERS = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_DEBUGGER = new ImBoolean(false);
    private static final ImBoolean SHOW_PROFILER = new ImBoolean(false);

    private static final ChunkVec RESET_CHUNK = new ChunkVec(17, 4, 18);
    protected static final String[] keys = {"A", "B", "C"};
    protected static final Double[] values = {0.1, 0.3, 0.6};
    private static final Vector3 TRANSLATE_TMP = new Vector3();
    private static final Vector3 SCALE_TMP = new Vector3();
    private static final Quaternion ROTATE_TMP = new Quaternion();

    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    private static boolean isImplCreated;
    private static boolean isContextCreated;
    private static final GuiEditor guiEditor = new GuiEditor();
    private static boolean triggerLoadWorld;
    private static ImPlotContext imPlotCtx;
    private static String[] modelViewerList = new String[0];

    public static void setupImGui() {
        if (GamePlatform.get().isAngleGLES()) return;

        QuantumClient.LOGGER.info("Setting up ImGui");

        QuantumClient.get().deferClose(GLFWErrorCallback.create((error, description) -> QuantumClient.LOGGER.error("GLFW Error: %s", description)).set());
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        synchronized (ImGuiOverlay.class) {
            ImGui.createContext();
            ImGuiOverlay.imPlotCtx = ImPlot.createContext();
            ImGuiOverlay.isContextCreated = true;
        }
        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();

        long windowHandle = DesktopLauncher.getGameWindow().getHandle();

        QuantumClient.invokeAndWait(() -> {
            ImGuiOverlay.imGuiGlfw.init(windowHandle, true);
            ImGuiOverlay.imGuiGl3.init("#version 140");
        });
    }

    public static void preInitImGui() {
        if (GamePlatform.get().isAngleGLES()) return;

        synchronized (ImGuiOverlay.class) {
            ImGuiOverlay.imGuiGlfw = new ImGuiImplGlfw();
            ImGuiOverlay.imGuiGl3 = new ImGuiImplGl3();
            ImGuiOverlay.isImplCreated = true;
        }
    }

    public static boolean isChunkSectionBordersShown() {
        return ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.get();
    }

    public static void renderImGui(QuantumClient client) {
        if (!ImGuiOverlay.SHOW_IM_GUI.get()) return;
        if (GamePlatform.get().isAngleGLES()) return;

        ImGuiOverlay.imGuiGlfw.newFrame();

        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(client.getWidth(), 18);
        ImGui.setNextWindowCollapsed(true);

        if (Gdx.input.isCursorCatched()) {
            ImGui.getIO().setMouseDown(new boolean[5]);
            ImGui.getIO().setMousePos(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        ImGuiOverlay.renderDisplay();

        if (ImGui.begin("MenuBar", ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.AlwaysAutoResize |
                ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.MenuBar |
                ImGuiInputTextFlags.AllowTabInput)) {
            ImGuiOverlay.renderMenuBar();
            ImGui.end();
        }

        ImGuiOverlay.renderWindows(client);

        ImGuiOverlay.handleTriggers();

        ImGui.render();
        ImGuiOverlay.imGuiGl3.renderDrawData(ImGui.getDrawData());

        ImGuiOverlay.handleInput();
    }

    private static void renderDisplay() {
        if (ImGuiFileDialog.display("Main::loadWorld", ImGuiFileDialogFlags.None, 200, 400, 800, 600)) {
            if (ImGuiFileDialog.isOk()) {
                Path filePathName = Path.of(ImGuiFileDialog.getFilePathName());
                QuantumClient.invoke(() -> QuantumClient.get().startWorld(filePathName));
            }
            ImGuiFileDialog.close();
        }
    }

    private static void handleTriggers() {
        if (ImGuiOverlay.triggerLoadWorld) {
            ImGuiOverlay.triggerLoadWorld = false;
            ImGuiFileDialog.openModal("Main::loadWorld", "Choose Folder", null, QuantumClient.getGameDir().toAbsolutePath().toString(), "", 1, 7, ImGuiFileDialogFlags.None);
        }
    }

    private static void renderWindows(QuantumClient client) {
        if (ImGuiOverlay.SHOW_PLAYER_UTILS.get()) ImGuiOverlay.showPlayerUtilsWindow(client);
        if (ImGuiOverlay.SHOW_GUI_UTILS.get()) ImGuiOverlay.showGuiEditor(client);
        if (ImGuiOverlay.SHOW_UTILS.get()) ImGuiOverlay.showUtils(client);
        if (ImGuiOverlay.SHOW_CHUNK_DEBUGGER.get()) ImGuiOverlay.showChunkDebugger(client);
        if (ImGuiOverlay.SHOW_SHADER_EDITOR.get()) ImGuiOverlay.showShaderEditor();
        if (ImGuiOverlay.SHOW_SKYBOX_EDITOR.get()) ImGuiOverlay.showSkyboxEditor();
        if (ImGuiOverlay.SHOW_MODEL_VIEWER.get()) ImGuiOverlay.showModelViewer();
    }

    private static void showModelViewer() {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Model Viewer", ImGuiOverlay.getDefaultFlags())) {
            if (ImGui.button("Reload")) {
                modelViewerList = QuantumClient.get().entityModelManager.getRegistry().keySet().stream().map(EntityType::getId).map(Objects::toString).sorted(String.CASE_INSENSITIVE_ORDER).toArray(String[]::new);
            }

            ImGui.text("Select Model:");
            ImGui.sameLine();
            ImGui.listBox("##ModelViewer::ListBox", MODEL_VIEWER_LIST_INDEX, modelViewerList);

            if (modelViewerList.length == 0) {
                ImGui.text("No models found");
            } else {

                String s = modelViewerList[MODEL_VIEWER_LIST_INDEX.get()];
                NamespaceID id = new NamespaceID(s);
                EntityType<?> entityType = Registries.ENTITY_TYPE.get(id);
                if (entityType != null) {
                    Model model = QuantumClient.get().entityModelManager.getFinished(entityType);
                    if (model != null) {
                        if (ImGui.treeNode("Model")) {
                            ImGui.text("Model Name:");
                            ImGui.sameLine();
                            ImGui.text(s);

                            if (ImGui.treeNode("Nodes")) {
                                for (Node node : model.nodes) {
                                    drawNode(node);
                                }

                                ImGui.treePop();
                            }

                            ImGui.treePop();
                        }
                    }
                }
            }

            if (ImGui.button("Close")) {
                ImGuiOverlay.SHOW_MODEL_VIEWER.set(false);
            }

        }
        ImGui.end();
    }

    private static void drawNode(Node node) {
        if (ImGui.treeNode(node.id)) {
            ImGui.text("Name:");
            ImGui.sameLine();
            ImGui.text(node.id);

            ImGui.text("Local Transform:");
            ImGui.treePush();
            drawTransform(node.localTransform, node);
            ImGui.treePop();

            ImGui.text("Global Transform:");
            ImGui.treePush();
            drawTransform(node.globalTransform, node);
            ImGui.treePop();

            for (Node child : node.getChildren()) {
                drawNode(child);
            }

            ImGui.treePop();
        }
    }

    private static void drawTransform(Matrix4 node, Node node1) {
        Vector3 translation = node.getTranslation(TRANSLATE_TMP);
        drawVec3("Translation:", translation);

        Vector3 scale = node1.localTransform.getScale(SCALE_TMP);
        drawVec3("Scale:", scale);

        Quaternion rotation = node1.localTransform.getRotation(ROTATE_TMP);
        ImGui.text("Rotation:");
        ImGui.sameLine();
        ImGui.text("X: " + rotation.x + " Y: " + rotation.y + " Z: " + rotation.z + " W: " + rotation.w);
    }

    private static void drawVec3(String name, Vector3 vec3) {
        ImGui.text(name);
        ImGui.sameLine();
        ImGui.text("X: " + vec3.x + " Y: " + vec3.y + " Z: " + vec3.z);
    }

    private static void handleInput() {
        if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.O))
            ImGuiOverlay.triggerLoadWorld = true;
        else if (Gdx.input.isKeyJustPressed(Input.Keys.P))
            ImGuiOverlay.SHOW_PLAYER_UTILS.set(!ImGuiOverlay.SHOW_PLAYER_UTILS.get());
        else if (Gdx.input.isKeyJustPressed(Input.Keys.G))
            ImGuiOverlay.SHOW_GUI_UTILS.set(!ImGuiOverlay.SHOW_GUI_UTILS.get());
        else if (Gdx.input.isKeyJustPressed(Input.Keys.F4))
            ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.set(!ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.get());
    }

    private static void renderMenuBar() {
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Load World...", "Ctrl+O")) {
                    ImGuiOverlay.triggerLoadWorld = true;
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Edit")) {
                ImGui.menuItem("Player Editor", "Ctrl+P", ImGuiOverlay.SHOW_PLAYER_UTILS);
                ImGui.menuItem("Gui Editor", "Ctrl+G", ImGuiOverlay.SHOW_GUI_UTILS);
                ImGui.menuItem("Shader Editor", "", ImGuiOverlay.SHOW_SHADER_EDITOR);
                ImGui.menuItem("Skybox Editor (Deprecated)", "", ImGuiOverlay.SHOW_SKYBOX_EDITOR);
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("View")) {
                ImGui.menuItem("Utils", null, ImGuiOverlay.SHOW_UTILS);
                ImGui.menuItem("Chunks", null, ImGuiOverlay.SHOW_CHUNK_DEBUGGER);
                ImGui.menuItem("Chunk Node Borders", "Ctrl+F4", ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS);
                ImGui.menuItem("InspectionRoot", "Ctrl+P", ImGuiOverlay.SHOW_PROFILER);
                ImGui.menuItem("Render Pipeline", null, ImGuiOverlay.SHOW_RENDER_PIPELINE);
                ImGui.menuItem("Model Viewer", null, ImGuiOverlay.SHOW_MODEL_VIEWER);
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Gizmos")) {
                @Nullable ClientWorldAccess terrainRenderer = QuantumClient.get().world;
                if (terrainRenderer instanceof ClientWorld world){
                    for (String category : world.getGizmoCategories()) {
                        if (ImGui.menuItem("Gizmo '" + category + "'", null, world.isGimzoCategoryEnabled(category))) {
                            world.toggleGizmoCategory(category);
                        }
                    }
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Resources")) {
                if (ImGui.menuItem("Reload Resources", "F1+R")) {
                    QuantumClient.get().reloadResourcesAsync();
                }
                ImGui.endMenu();
            }

            ImGui.text(" FPS: " + Gdx.graphics.getFramesPerSecond() + " ");
            ImGui.sameLine();
            ImGui.text(" Client TPS: " + Gdx.graphics.getFramesPerSecond() + " ");
            ImGui.sameLine();
            QuantumServer server = QuantumServer.get();
            if (server != null) {
                ImGui.text(" Server TPS: " + server.getCurrentTps() + " ");
                ImGui.sameLine();
            }
            ImGui.text(" Frame ID: " + Gdx.graphics.getFrameId() + " ");
            ImGui.endMenuBar();
        }
    }

    private static void showChunkDebugger(QuantumClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (client.player != null && ImGui.begin("Chunk Debugging", ImGuiOverlay.getDefaultFlags())) {
            if (ImGui.button(String.format("Reset chunk at %s", ImGuiOverlay.RESET_CHUNK))) {
                CompletableFuture.runAsync(() -> {
                    @Nullable ClientWorldAccess world = client.world;
                    QuantumClient.invokeAndWait(() -> {
                        if (world != null) {
                            world.unloadChunk(ImGuiOverlay.RESET_CHUNK);
                        }
                    });
                    QuantumServer.invokeAndWait(() -> client.integratedServer.getWorld().regenerateChunk(ImGuiOverlay.RESET_CHUNK));
                });
            }
            ImGui.end();
        }
    }

    private static void showShaderEditor() {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Shader Editor", ImGuiOverlay.getDefaultFlags())) {
            if (ImGui.treeNode("Shader::SSAO", "SSAO")) {
                ImGuiEx.editFloat("iGamma", "Shader::SSAO::iGamma", ImGuiOverlay.I_GAMMA::get, ImGuiOverlay.I_GAMMA::set);
                ImGui.treePop();
            }

            if (ImGui.treeNode("Shader::SkyBox", "SkyBox")) {
                ImGuiEx.editColor3Gdx("DayTopColor", "Shader::SkyBox::DayTopColor", () -> ClientWorld.DAY_TOP_COLOR);
                ImGuiEx.editColor3Gdx("DayBottomColor", "Shader::SkyBox::DayBottomColor", () -> ClientWorld.DAY_BOTTOM_COLOR);
                ImGuiEx.editColor3Gdx("NightTopColor", "Shader::SkyBox::NightTopColor", () -> ClientWorld.NIGHT_TOP_COLOR);
                ImGuiEx.editColor3Gdx("NightBottomColor", "Shader::SkyBox::NightBottomColor", () -> ClientWorld.NIGHT_BOTTOM_COLOR);
                ImGuiEx.editColor3Gdx("SunRiseSetColor", "Shader::SkyBox::SunRiseSetColor", () -> ClientWorld.SUN_RISE_COLOR);
                ImGuiEx.editBool("Debug", "Shader::SkyBox::Debug", () -> Skybox.debug, b -> Skybox.debug = b);
                ImGuiEx.editFloat("Rotation", "Shader::SkyBox::Rotation", ClientWorld.SKYBOX_ROTATION::getDegrees, ImGuiOverlay::setSkyboxRot);
                ImGui.treePop();
            }

            if (ImGui.treeNode("Shader::World", "World")) {
                ImGuiEx.editColor3("FogColor", "Shader::World::FogColor", ClientWorld.FOG_COLOR::get, ClientWorld.FOG_COLOR::set);
                ImGuiEx.editDouble("FogDensity", "Shader::World::FogDensity", ClientWorld.FOG_DENSITY::get, ClientWorld.FOG_DENSITY::set);
                ImGuiEx.editDouble("FogStart", "Shader::World::FogStart", ClientWorld.FOG_START::get, ClientWorld.FOG_START::set);
                ImGuiEx.editDouble("FogEnd", "Shader::World::FogEnd", ClientWorld.FOG_END::get, ClientWorld.FOG_END::set);
                ImGuiEx.editVec2f("AtlasSize", "Shader::World::AtlasSize", ClientWorld.ATLAS_SIZE::get, ClientWorld.ATLAS_SIZE::set);
                ImGuiEx.editVec3f("CameraUp", "Shader::World::CameraUp", () -> new Vec3f(WorldShader.CAMERA_UP.x, WorldShader.CAMERA_UP.y, WorldShader.CAMERA_UP.z), vec3f -> WorldShader.CAMERA_UP.set(vec3f.x, vec3f.y, vec3f.z));
                ImGui.treePop();
            }

            ImGui.end();
        }
    }

    private static void showSkyboxEditor() {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Skybox Editor (Deprecated)", ImGuiOverlay.getDefaultFlags())) {
            ImGuiEx.editColor3Gdx("DayTopColor", "Shader::SkyBox::DayTopColor", () -> ClientWorld.DAY_TOP_COLOR);
            ImGuiEx.editColor3Gdx("DayBottomColor", "Shader::SkyBox::DayBottomColor", () -> ClientWorld.DAY_BOTTOM_COLOR);
            ImGuiEx.editColor3Gdx("NightTopColor", "Shader::SkyBox::NightTopColor", () -> ClientWorld.NIGHT_TOP_COLOR);
            ImGuiEx.editColor3Gdx("NightBottomColor", "Shader::SkyBox::NightBottomColor", () -> ClientWorld.NIGHT_BOTTOM_COLOR);
            ImGuiEx.editColor3Gdx("SunRiseSetColor", "Shader::SkyBox::SunRiseSetColor", () -> ClientWorld.SUN_RISE_COLOR);
            ImGuiEx.editBool("Debug", "Shader::SkyBox::Debug", () -> Skybox.debug, b -> Skybox.debug = b);
            ImGuiEx.editFloat("Rotation", "Shader::SkyBox::Rotation", ClientWorld.SKYBOX_ROTATION::getDegrees, ImGuiOverlay::setSkyboxRot);
            ImGui.end();
        }
    }

    private static void showPlayerUtilsWindow(QuantumClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (client.player != null && ImGui.begin("Player Utils", ImGuiOverlay.getDefaultFlags())) {
            ImGuiEx.text("Id:", client.player::getId);
            ImGuiEx.text("Dead:", client.player::isDead);
            ImGuiEx.editFloat("Walking Speed:", "PlayerWalkingSpeed", client.player::getWalkingSpeed, client.player::setWalkingSpeed);
            ImGuiEx.editFloat("Flying Speed:", "PlayerFlyingSpeed", client.player::getFlyingSpeed, client.player::setFlyingSpeed);
            ImGuiEx.editFloat("Gravity:", "PlayerGravity", () -> client.player.gravity, v -> client.player.gravity = v);
            ImGuiEx.editFloat("Jump Velocity:", "PlayerJumpVelocity", () -> client.player.jumpVel, v -> client.player.jumpVel = v);
            ImGuiEx.editFloat("Health:", "PlayerHealth", client.player::getHealth, client.player::setHealth);
            ImGuiEx.editFloat("Max Health:", "PlayerMaxHealth", client.player::getMaxHealth, client.player::setMaxHealth);
            ImGuiEx.editBool("No Gravity:", "PlayerNoGravity", () -> client.player.noGravity, v -> client.player.noGravity = v);
            ImGuiEx.editBool("Flying:", "PlayerFlying", client.player::isFlying, client.player::setFlying);
            ImGuiEx.editBool("Allow Flight:", "PlayerAllowFlight", client.player::isAllowFlight, v -> {});
            ImGuiEx.bool("On Ground:", () -> client.player.onGround);
            ImGuiEx.bool("Colliding:", () -> client.player.isColliding);
            ImGuiEx.bool("Colliding X:", () -> client.player.isCollidingX);
            ImGuiEx.bool("Colliding Y:", () -> client.player.isCollidingY);
            ImGuiEx.bool("Colliding Z:", () -> client.player.isCollidingZ);

            if (ImGui.collapsingHeader("Position")) {
                ImGui.treePush();
                ImGuiEx.editDouble("X:", "PlayerX", client.player::getX, v -> client.player.setX(v));
                ImGuiEx.editDouble("Y:", "PlayerY", client.player::getY, v -> client.player.setY(v));
                ImGuiEx.editDouble("Z:", "PlayerZ", client.player::getZ, v -> client.player.setZ(v));
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Velocity")) {
                ImGui.treePush();
                ImGuiEx.editDouble("X:", "PlayerVelocityX", () -> client.player.velocityX, v -> client.player.velocityX = v);
                ImGuiEx.editDouble("Y:", "PlayerVelocityY", () -> client.player.velocityY, v -> client.player.velocityY = v);
                ImGuiEx.editDouble("Z:", "PlayerVelocityZ", () -> client.player.velocityZ, v -> client.player.velocityZ = v);
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Rotation")) {
                ImGui.treePush();
                ImGuiEx.editFloat("X:", "PlayerXRot", client.player::getXRot, v -> client.player.setXRot(v));
                ImGuiEx.editFloat("Y:", "PlayerYRot", client.player::getYRot, v -> client.player.setYRot(v));
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Player Input")) {
                ImGui.treePush();
                ImGuiEx.bool("Forward", () -> client.playerInput.forward);
                ImGuiEx.bool("Backward", () -> client.playerInput.backward);
                ImGuiEx.bool("Left", () -> client.playerInput.strafeLeft);
                ImGuiEx.bool("Right", () -> client.playerInput.strafeRight);
                ImGuiEx.bool("Up", () -> client.playerInput.up);
                ImGuiEx.bool("Down", () -> client.playerInput.down);
                ImGui.treePop();
            }
            ImGui.end();
        }
    }

    private static void showGuiEditor(QuantumClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("GUI Editor", ImGuiOverlay.getDefaultFlags())) {
            ImGuiOverlay.guiEditor.render(client);
        }
        ImGui.end();
    }

    private static void showUtils(QuantumClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Utils", ImGuiOverlay.getDefaultFlags())) {
            ImGuiEx.slider("FOV", "GameFOV", (int) client.camera.fov, 10, 150, i -> client.camera.fov = i);
        }
        ImGui.end();
    }

    private static int getDefaultFlags() {
        boolean cursorCaught = Gdx.input.isCursorCatched();
        var flags = ImGuiWindowFlags.None;
        if (cursorCaught) flags |= ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoInputs;
        return flags;
    }

    public static boolean isShown() {
        return ImGuiOverlay.SHOW_IM_GUI.get();
    }

    public static void setShowingImGui(boolean value) {
        ImGuiOverlay.SHOW_IM_GUI.set(value);
    }

    public static boolean isProfilerShown() {
        return ImGuiOverlay.SHOW_PROFILER.get();
    }

    public static void dispose() {
        if (GamePlatform.get().isAngleGLES()) return;

        synchronized (ImGuiOverlay.class) {
            if (ImGuiOverlay.isImplCreated) {
                ImGuiOverlay.imGuiGl3.dispose();
                ImGuiOverlay.imGuiGlfw.dispose();
            }

            if (ImGuiOverlay.isContextCreated) {
                ImGui.destroyContext();
                ImPlot.destroyContext(ImGuiOverlay.imPlotCtx);
            }
        }
    }

    private static void setSkyboxRot(float v) {
        ClientWorld.SKYBOX_ROTATION = Rot.deg(v);
    }
}
