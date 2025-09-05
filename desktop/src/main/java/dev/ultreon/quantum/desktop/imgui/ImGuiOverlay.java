package dev.ultreon.quantum.desktop.imgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.math.*;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.DevFlag;
import dev.ultreon.quantum.GameInsets;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.block.property.StatePropertyKey;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.widget.UIContainer;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.client.shaders.WorldShader;
import dev.ultreon.quantum.client.util.Rot;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.client.world.Skybox;
import dev.ultreon.quantum.component.Component;
import dev.ultreon.quantum.component.GameComponent;
import dev.ultreon.quantum.debug.profiler.ProfileData;
import dev.ultreon.quantum.debug.profiler.Profiler;
import dev.ultreon.quantum.debug.profiler.Section;
import dev.ultreon.quantum.debug.profiler.ThreadSection;
import dev.ultreon.quantum.desktop.DesktopMain;
import dev.ultreon.quantum.dev.DevPipe;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.resources.ResourceCategory;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.resources.StaticResource;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.RegionVec;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.ImVec2;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorCoordinates;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.*;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@SuppressWarnings("t")
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
    private static boolean crashHooked = false;
    private static Throwable crash = null;
    public static final Consumer<Throwable> CRASH_HOOK = (t) -> {
        GamePlatform.get().disableGame();
        crash = t;
    };
    public static final ImInt SHADER_DEBUG_STATE = new ImInt(0);
    public static String NET_PIPE_OUT = "";
    public static final DevPipe DEV_PIPE = (tag, message) -> {
        if (tag.equals("NetLog")) {
            String s = NET_PIPE_OUT;
            NET_PIPE_OUT += message + "\n";
            if (NET_PIPE_OUT.length() > 1024) {
                NET_PIPE_OUT = NET_PIPE_OUT.substring(NET_PIPE_OUT.indexOf("\n") + 1);
            }
        } else {
            LoggerFactory.getLogger(ImGuiOverlay.class).warn("Unhandled DevPipe: {}", tag);
        }
    };
    private static final ImBoolean SHOW_IM_GUI = new ImBoolean(false);
    private static final ImBoolean SHOW_PLAYER_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_GUI_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_SHADER_EDITOR = new ImBoolean(false);
    private static final ImBoolean SHOW_SKYBOX_EDITOR = new ImBoolean(false);
    private static final ImBoolean SHOW_MODEL_VIEWER = new ImBoolean(false);
    private static final ImBoolean SHOW_HIDDEN_FIELDS = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_SECTION_BORDERS = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_DEBUGGER = new ImBoolean(false);
    private static final ImBoolean SHOW_PROFILER = new ImBoolean(false);
    private static final ImBoolean SHOW_OCCLUSION_DEBUG = new ImBoolean(false);
    private static final ImBoolean SHOW_NETWORK_LOGGING = new ImBoolean(false);

    private static final ImBoolean SHOW_ABOUT = new ImBoolean(false);
    private static final ImBoolean SHOW_METRICS = new ImBoolean(false);
    private static final ImBoolean SHOW_STACK_TOOL = new ImBoolean(false);
    private static final ImBoolean SHOW_STYLE_EDITOR = new ImBoolean(false);
    private static final ImBoolean SHOW_JSHELL = new ImBoolean(false);
    private static final ImBoolean SHOW_CLASS_ATTACHER = new ImBoolean(false);

    protected static final String[] keys = {"A", "B", "C"};
    protected static final Double[] values = {0.1, 0.3, 0.6};
    private static final Vector3 TRANSLATE_TMP = new Vector3();
    private static final Vector3 SCALE_TMP = new Vector3();
    private static final Quaternion ROTATE_TMP = new Quaternion();
    public static final boolean[] MOUSE_DOWN = new boolean[5];

    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    private static boolean isImplCreated;
    private static boolean isContextCreated;
    private static final GuiEditor guiEditor = new GuiEditor();
    private static boolean triggerLoadWorld;
    private static ImPlotContext imPlotCtx;
    private static String[] modelViewerList = new String[0];

    @SuppressWarnings("GDXJavaStaticResource")
    private static Object selected = null;
    private static final GameInsets bounds = new GameInsets();
    private static final ImInt rotType = new ImInt(0);
    public static final Map<NamespaceID, TextEditor> textEditors = new HashMap<>();
    public static TextEditorLanguageDefinition glsl;
    public static final Map<NamespaceID, TextEditorCoordinates> textEditorPos = new HashMap<>();
    private static boolean firstLoop = true;
    private static final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private static final PrintStream ps = new PrintStream(baos);
    private static Context jshell;
    private static final ImString inputBuffer = new ImString(512);
    private static final ImString inputBuffer1 = new ImString(512);
    private static int selectedIndex;
    private static boolean focusInput;
    private static final List<String> filteredClasses = new ArrayList<>();
    private static Class<?> selectedClass;
    private static long nextProfilerCollect;
    private static ProfileData profilerData;
    private static List<Thread> threads;

    public static void setupImGui() {
        if (GamePlatform.get().isAngleGLES()) {
            LoggerFactory.getLogger(ImGuiOverlay.class).trace("ImGui Disabled for Angle GLES");
            return;
        }

        QuantumClient.LOGGER.info("Setting up ImGui");

        QuantumClient.get().deferClose(GLFWErrorCallback.create((error, description) -> QuantumClient.LOGGER.error("GLFW Error: {}", description)).set());
        synchronized (ImGuiOverlay.class) {
            ImGui.createContext();
            ImGuiOverlay.imPlotCtx = ImPlot.createContext();
            ImGuiOverlay.isContextCreated = true;
        }
        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();

        // This enables FreeType font renderer, which is disabled by default.
        io.getFonts().setFreeTypeRenderer(true);


        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);  // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);      // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);    // Enable Multi-Viewport / Platform Windows

        long windowHandle = DesktopMain.getGameWindow().getHandle();

        QuantumClient.invokeAndWait(() -> {
            ImGuiOverlay.imGuiGlfw.init(windowHandle, true);
            ImGuiOverlay.imGuiGl3.init("#version 140");

            glsl = TextEditorLanguageDefinition.GLSL();
        });
    }

    public static void preInitImGui() {
        if (GamePlatform.get().isAngleGLES()) {
            LoggerFactory.getLogger(ImGuiOverlay.class).trace("ImGui Disabled for Angle GLES");
            return;
        }

        LoggerFactory.getLogger(ImGuiOverlay.class).info("Pre-initializing ImGui");
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
        if (GamePlatform.get().isAngleGLES()) {
            LoggerFactory.getLogger(ImGuiOverlay.class).trace("ImGui Disabled for Angle GLES");
            return;
        }

        if (Gdx.input.isCursorCatched()) {
            ImGui.getIO().setMousePos(Float.MAX_VALUE, Float.MAX_VALUE);
        }

        newFrame();

        process(client);

        endFrame();
    }

    private static void newFrame() {
        imGuiGl3.newFrame();
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    private static void endFrame() {
        ImGui.render();
        ImGuiOverlay.imGuiGl3.renderDrawData(ImGui.getDrawData());

        // Update and Render additional Platform Windows
        // (Platform functions may change the current OpenGL context, so we save/restore it to make it easier to paste this code elsewhere.
        //  For this specific demo app we could also call glfwMakeContextCurrent(window) directly)
        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupCurrentContext = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            GLFW.glfwMakeContextCurrent(backupCurrentContext);
        }

        ImGuiOverlay.handleInput();
    }

    private static void process(QuantumClient client) {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);

        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX(), ImGui.getMainViewport().getPosY() + 18);
        ImGui.setNextWindowSize(ImGui.getMainViewport().getSizeX(), ImGui.getMainViewport().getSizeY() - 18);
        ImGui.setNextWindowCollapsed(false);

        ImGui.getStyle().setWindowPadding(0, 0);
        ImGui.getStyle().setWindowBorderSize(0);

        ImGui.begin("MainDockingArea", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoScrollbar);
        int id = ImGui.getID("MainDockingArea");
        int dockSpace = ImGui.dockSpace(id);
        ImGui.end();

        if (firstLoop) {
            firstLoop = false;

            ImInt gameDockId = new ImInt(dockSpace);

            ImInt sceneDock = new ImInt(imgui.internal.ImGui.dockBuilderSplitNode(gameDockId.get(), ImGuiDir.Left, 0.15f, null, gameDockId));
            ImInt nodeDock = new ImInt(imgui.internal.ImGui.dockBuilderSplitNode(gameDockId.get(), ImGuiDir.Right, 0.3f, null, gameDockId));
            ImInt assetDock = new ImInt(imgui.internal.ImGui.dockBuilderSplitNode(gameDockId.get(), ImGuiDir.Down, 0.3f, null, gameDockId));
            imgui.internal.ImGui.dockBuilderDockWindow("Node View", nodeDock.get());
            imgui.internal.ImGui.dockBuilderDockWindow("Scene View", sceneDock.get());
            imgui.internal.ImGui.dockBuilderDockWindow("Asset View", assetDock.get());
            imgui.internal.ImGui.dockBuilderDockWindow("Game", gameDockId.get());
            imgui.internal.ImGui.dockBuilderFinish(gameDockId.get());
        }


        ImGui.getStyle().setWindowPadding(8, 8);
        ImGui.getStyle().setWindowBorderSize(1);
        renderWindows(client);

        ImGui.setNextWindowPos(ImGui.getMainViewport().getPos());
        ImGui.setNextWindowSize(ImGui.getMainViewport().getSizeX(), 18);
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
                ImGuiWindowFlags.NoDocking |
                ImGuiWindowFlags.NoDecoration |
                ImGuiInputTextFlags.AllowTabInput)) {
            ImGuiOverlay.renderMenuBar();
        }
        ImGui.end();

        ImGuiOverlay.handleTriggers();
    }

    private static void renderDisplay() {
        if (ImGuiFileDialog.display("Main::loadWorld", ImGuiFileDialogFlags.None, 200, 400, 800, 600)) {
            if (ImGuiFileDialog.isOk()) {
                Path filePathName = Path.of(ImGuiFileDialog.getFilePathName());
                QuantumClient.invoke(() -> QuantumClient.get().startWorld(new FileHandle(filePathName.toFile())));
            }
            ImGuiFileDialog.close();
        }
    }

    private static void handleTriggers() {
        if (ImGuiOverlay.triggerLoadWorld) {
            ImGuiOverlay.triggerLoadWorld = false;
            ImGuiFileDialog.openModal("Main::loadWorld", "Choose Folder", null, QuantumClient.getGameDir().path(), "", 1, 7, ImGuiFileDialogFlags.None);
        }
    }

    private static void renderWindows(QuantumClient client) {
        showSceneView();
        showAssetView(client);
        showNodeView(client);
        showGame(client);

        if (ImGuiOverlay.SHOW_ABOUT.get()) ImGui.showAboutWindow();
        if (ImGuiOverlay.SHOW_METRICS.get()) ImGui.showMetricsWindow();
        if (ImGuiOverlay.SHOW_STACK_TOOL.get()) ImGui.showStackToolWindow();
        if (ImGuiOverlay.SHOW_STYLE_EDITOR.get()) ImGui.showStyleEditor();

        if (ImGuiOverlay.SHOW_PLAYER_UTILS.get()) ImGuiOverlay.showPlayerUtilsWindow(client);
        if (ImGuiOverlay.SHOW_GUI_UTILS.get()) ImGuiOverlay.showGuiEditor(client);
        if (ImGuiOverlay.SHOW_UTILS.get()) ImGuiOverlay.showUtils(client);
        if (ImGuiOverlay.SHOW_SHADER_EDITOR.get()) ImGuiOverlay.showShaderEditor();
        if (ImGuiOverlay.SHOW_SKYBOX_EDITOR.get()) ImGuiOverlay.showSkyboxEditor();
        if (ImGuiOverlay.SHOW_MODEL_VIEWER.get()) ImGuiOverlay.showModelViewer();
        if (ImGuiOverlay.SHOW_CLASS_ATTACHER.get()) showClassAttacher();
        if (ImGuiOverlay.SHOW_NETWORK_LOGGING.get()) ImGuiOverlay.showNetworkLogging();
        if (ImGuiOverlay.SHOW_JSHELL.get()) {
            if (jshell == null) {
                jshell = Context.newBuilder("js").allowAllAccess(true).out(ps).in(InputStream.nullInputStream()).err(ps).build();
            }
            ImGuiOverlay.showJShell(jshell);
        } else if (jshell != null) {
            jshell.close();
            jshell = null;
        }
    }

    private static void showNetworkLogging() {
        if (ImGui.begin("Network Logging")) {
            ImGui.beginChild("##network_log_area", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY(), true);
            ImGui.text(NET_PIPE_OUT);
            ImGui.endChild();
        }
        ImGui.end();
    }

    private static void showClassAttacher() {
        ImGui.begin("Class Attacher");

        // Input Field
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
        ImGui.inputText("##class_input", inputBuffer1, ImGuiInputTextFlags.AutoSelectAll);

        if (focusInput) {
            ImGui.setKeyboardFocusHere(-1);
            focusInput = false;
        }

        updateFiltered();

        // Suggestions
        if (!filteredClasses.isEmpty()) {
            ImGui.beginChild("##suggestions", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY(), true);
            for (int i = 0; i < filteredClasses.size(); i++) {
                boolean isSelected = i == selectedIndex;
                if (ImGui.selectable(filteredClasses.get(i), isSelected)) {
                    inputBuffer1.set(filteredClasses.get(i));
                    filteredClasses.clear(); // Hide suggestions after selection
                    selectedIndex = -1;
                }
                if (isSelected && ImGui.isItemFocused()) {
                    if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) { // Backspace
                        inputBuffer1.set("");
                        filteredClasses.clear();
                    }
                }
            }

            ImGui.endChild();

            // Keyboard navigation
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) { // Down
                selectedIndex = (selectedIndex + 1) % filteredClasses.size();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) { // Up
                selectedIndex = (selectedIndex - 1 + filteredClasses.size()) % filteredClasses.size();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) { // Enter
                if (selectedIndex >= 0 && selectedIndex < filteredClasses.size()) {
                    String value = filteredClasses.get(selectedIndex);
                    inputBuffer1.set(value);
                    try {
                        selectedClass = Class.forName(value);
                    } catch (ClassNotFoundException e) {
                        CommonConstants.LOGGER.error("Unable to load already loaded class " + value, e);
                    }
                    filteredClasses.clear();
                    selectedIndex = -1;
                }
            }
        }

        ImGui.end();
    }

    private static void updateFiltered() {
        Class<?>[] allClasses = GamePlatform.get().getLoadedClasses();
        filteredClasses.clear();
        if (!inputBuffer1.isEmpty()) {
            List<String> toSort = new ArrayList<>();
            for (Class<?> cls : allClasses) {
                String name = cls.getName();
                if (name.startsWith("[")) continue;
                if (name.toLowerCase().contains(inputBuffer1.get().toLowerCase())) {
                    toSort.add(name);
                }
            }
            toSort.sort(null);
            List<String> list = new ArrayList<>();
            long limit = 100;
            for (String cls : toSort) {
                if (limit-- == 0) break;
                list.add(cls);
            }
            filteredClasses.addAll(
                    list
            );
        }
    }

    private static void showJShell(Context jshell) {
        if (ImGui.begin("Game Shell")) {
            // Output area
            ImGui.begin("ConsoleOutput");
            float scrollY = ImGui.getScrollY();
            ImGui.textWrapped(baos.toString());
            if (scrollY == 1.0f) ImGui.setScrollHereY(1.0f); // Auto-scroll
            ImGui.end();

            // Input field
            ImGui.separator();
            ImGui.text("Enter Java expression:");
            ImGui.inputTextMultiline("##input", inputBuffer, 512);
            if (ImGui.isItemFocused() && Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && Gdx.input.isKeyJustPressed(Input.Keys.ALT_LEFT) && !inputBuffer.get().isEmpty()) {
                evaluateShell(jshell);
            }

            // Evaluate button
            if (ImGui.button("Evaluate")) {
                evaluateShell(jshell);
            }

            ImGui.sameLine();
            if (ImGui.button("Clear Output")) {
                baos.reset();
            }
        }
        ImGui.end();
    }

    private static void evaluateShell(Context jshell) {
        try {
            // Get JS bindings
            Value js = jshell.getBindings("js");
            
            // Assign host class
            Class<?> selClass = selectedClass;
            if (selClass != null)
                js.putMember("HostClass", jshell.eval("js", "Java.type('" + selClass.getCanonicalName() + "');"));

            // Assign host object
            Object sel = selected;
            if (sel != null)
                js.putMember("hostObject", Value.asValue(sel));

            // Evaluate JS expression
            Value java = jshell.eval("js", inputBuffer.get());
            try {
                String string = java.as(Object.class).toString() + "\n";
                baos.write(string.getBytes());
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Unable to write to output stream", e);
            }
        } catch (Exception e) {
            e.printStackTrace(ps);
        }
    }

    private static void showGame(QuantumClient ignoredClient) {
        ImGuiStyle style = ImGui.getStyle();

        // Save the original padding if needed
        ImVec2 originalPadding = new ImVec2(style.getWindowPadding());

        // Set the padding to 0 (or any value you need)
        style.setWindowPadding(0, 0);
        if (ImGui.begin("Game", ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoMove)) {
            bounds.left = (int) ((ImGui.getMousePosX() - ImGui.getCursorPosX() - ImGui.getWindowPosX()) * ImGui.getWindowDpiScale());
            bounds.top = (int) ((ImGui.getMousePosY() - ImGui.getCursorPosY() - ImGui.getWindowPosY()) * ImGui.getWindowDpiScale());
            float contentRegionAvailX = ImGui.getContentRegionAvailX();
            float contentRegionAvailY = ImGui.getContentRegionAvailY();
            bounds.right = (int) (contentRegionAvailX * ImGui.getWindowDpiScale());
            bounds.bottom = (int) (contentRegionAvailY * ImGui.getWindowDpiScale());

            ImGui.image(QuantumClient.get().targetFbo.getColorBufferTexture().getTextureObjectHandle(), contentRegionAvailX, contentRegionAvailY, 0, 1, 1, 0, 1, 1, 1, 1);
        }
        ImGui.end();

        // Restore the original padding
        style.setWindowPadding(originalPadding.x, originalPadding.y);
    }

    private static void showAssetView(QuantumClient client) {
        if (ImGui.begin("Asset View", ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoMove)) {
            // Show a list of all assets
            ResourceManager resourceManager = client.resourceManager;
            if (ImGui.treeNodeEx("Assets", ImGuiTreeNodeFlags.Framed | ImGuiTreeNodeFlags.OpenOnArrow | (resourceManager == null ? ImGuiTreeNodeFlags.Leaf : 0)) && resourceManager != null) {
                for (ResourceCategory category : resourceManager.getResourceCategories()) {
                    if (ImGui.treeNodeEx(category.getName(), ImGuiTreeNodeFlags.Framed | ImGuiTreeNodeFlags.OpenOnArrow)) {
                        for (Map.Entry<NamespaceID, StaticResource> entry : category.mapEntries().entrySet()) {
                            StaticResource resource = entry.getValue();
                            NamespaceID location = entry.getKey();
                            if (ImGui.treeNodeEx(location.toString(), ImGuiTreeNodeFlags.Framed | ImGuiTreeNodeFlags.OpenOnArrow)) {
                                if (location.getPath().endsWith(".png")) {
                                    Texture texture = QuantumClient.get().getTextureManager().getTexture(location, null);
                                    ImGui.image(texture.getTextureObjectHandle(), 64, 64, 0, 0, 1, 1);
                                } else if (location.getPath().endsWith(".frag")) {
                                    byte[] bytes = resource.loadOrGet();
                                    if (bytes != null) {
                                        String shader = new String(bytes, StandardCharsets.UTF_8);
                                        TextEditor textEditor = textEditors.get(location);
                                        if (textEditor == null) {
                                            textEditor = new TextEditor();
                                            textEditors.put(location, textEditor);
                                        }

                                        textEditor.setText(shader);
                                        textEditor.setReadOnly(true);
                                        textEditor.setLanguageDefinition(glsl);
                                        textEditor.setColorizerEnable(true);
                                        textEditor.setShowWhitespaces(false);

                                        TextEditorCoordinates coordinates = textEditorPos.get(location);
                                        if (coordinates != null) textEditor.setCursorPosition(coordinates);

                                        float v = textEditor.getTotalLines() * ImGui.getFont().getFontSize() + 16;
                                        textEditor.render("Shader Editor - " + location, ImGui.getContentRegionAvailX(), v);

                                        if (textEditor.isCursorPositionChanged()) {
                                            textEditorPos.put(location, textEditor.getCursorPosition());
                                        }


                                        if (ImGui.isItemHovered()) {
                                            ImGui.setTooltip("Click to copy to clipboard");
                                            if (ImGui.isItemClicked()) {
                                                ImGui.setClipboardText(shader);
                                            }
                                        }
                                    }
                                }
                                ImGui.treePop();
                            } else {
                                TextEditor remove = textEditors.remove(location);

                                if (remove != null) {
                                    remove.destroy();
                                }
                            }
                        }
                        ImGui.treePop();
                    }
                }
                ImGui.treePop();
            }
        }
        ImGui.end();
    }

    private static void showNodeView(QuantumClient ignoredClient) {
        if (ImGui.begin("Node View", ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoMove)) {
            Object sel = selected;

            if (sel != null) {
                if (ImGui.treeNode("Game Object")) {
                    renderComponent(sel);
                    ImGui.treePop();
                }

                if (sel instanceof GameNode) {
                    for (Component<?> component : ((GameNode) sel).getComponents()) {
                        if (ImGui.treeNode(component.getClass().getName(), component instanceof GameComponent ? component.getClass().getSimpleName() : component.getClass().getSimpleName() + (GamePlatform.get().isDevEnvironment() ? ".java" : ".class"))) {
                            renderComponent(component);
                            ImGui.treePop();
                        }
                    }
                }

            }
        }
        ImGui.end();
    }

    private static void renderComponent(final @Nullable Object component) {
        if (component == null) return;
        if (ImGui.beginTable("##<<Comp>> " + System.identityHashCode(component), 2, ImGuiTableFlags.BordersOuter | ImGuiTableFlags.SizingFixedFit | ImGuiTableFlags.RowBg)) {
            ImGui.tableSetupColumn("Field", ImGuiTableColumnFlags.WidthFixed, 100, 0);
            ImGui.tableSetupColumn("Value", ImGuiTableColumnFlags.WidthStretch, 1);
            ImGui.tableHeadersRow();
            ImGui.tableSetColumnIndex(0);
            ImGui.text("Field");
            ImGui.tableSetColumnIndex(1);
            ImGui.text("Value");

            Class<?> clazz = component.getClass();
            Set<Field> fields = new HashSet<>();
            Stack<Class<?>> stack = new Stack<>();
            while (true) {
                for (Field field : component.getClass().getDeclaredFields()) {
                    processField(component, field, fields);
                }
                for (Field field : component.getClass().getFields()) {
                    processField(component, field, fields);
                }
                for (Class<?> anInterface : clazz.getInterfaces()) {
                    if (!stack.contains(anInterface) && anInterface != Object.class)
                        stack.push(anInterface);
                }
                if (clazz.getSuperclass() != null && !stack.contains(clazz.getSuperclass()) && !clazz.getSuperclass().equals(Object.class))
                    stack.push(clazz.getSuperclass());
                if (stack.isEmpty()) break;
                clazz = stack.pop();
            }
        }
        ImGui.endTable();
    }

    private static void processField(@NotNull Object component, Field field, Set<Field> fields) {
        if (fields.contains(field)) return;
        fields.add(field);
        if ((!Modifier.isPublic(field.getModifiers()) && !field.isAnnotationPresent(ShowInNodeView.class)
                || field.isSynthetic()
                || field.isAnnotationPresent(HiddenNode.class))
                && !SHOW_HIDDEN_FIELDS.get()
                || Modifier.isStatic(field.getModifiers()))
            return;

        boolean readOnly = Modifier.isFinal(field.getModifiers());

        Runnable runnable = renderObject(component, field, readOnly);
        if (runnable != null) {
            ImGui.tableNextRow();
            ImGui.tableSetColumnIndex(0);
            ImGui.text(field.getName());
            ImGui.tableSetColumnIndex(1);
            runnable.run();
        }
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Runnable renderObject(@Nullable Object component, Field field, boolean readOnly) {
        try {
            field.setAccessible(true);
            Object object = field.get(component);
            if (field.getType().isPrimitive()) return () -> {
                if (object instanceof Number number) {
                    num(component, field, readOnly, number, object);
                } else if (object instanceof Boolean) {
                    ImBoolean b = new ImBoolean((boolean) object);
                    if (ImGui.checkbox(field.getName(), b) && !readOnly) {
                        try {
                            field.set(component, b.get());
                        } catch (IllegalAccessException e) {
                            // ignore
                        }
                    }
                    ImGui.sameLine(120);
                    ImGui.text((boolean) object ? "True" : "False");
                } else {
                    ImGui.text(String.valueOf(object));
                }
            };
            if (object instanceof GLTexture texture) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), "Texture")) {
                        ImGui.image(texture.getTextureObjectHandle(), ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailX());
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof TextureRegion region) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), "Texture Region")) {
                        ImGui.image(region.getTexture().getTextureObjectHandle(), ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailX(), region.getU(), region.getV(), region.getU2(), region.getV2());
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Material material) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), "Material")) {
                        if (ImGui.beginTable("##Material[" + System.identityHashCode(material), 2, ImGuiTableFlags.Borders)) {
                            ImGui.tableSetupColumn("Key", ImGuiTableColumnFlags.WidthFixed, 100, 0);
                            ImGui.tableSetupColumn("Value", ImGuiTableColumnFlags.WidthStretch, 1);
                            ImGui.tableHeadersRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text("Key");
                            ImGui.tableSetColumnIndex(1);
                            ImGui.text("Value");

                            for (Attribute attr : material) {
                                ImGui.tableNextRow();
                                ImGui.tableSetColumnIndex(0);
                                ImGui.text(Attribute.getAttributeAlias(attr.type));
                                ImGui.tableSetColumnIndex(1);

                                if (attr instanceof TextureAttribute textureAttribute) {
                                    TextureDescriptor<Texture> textureDescription = textureAttribute.textureDescription;
                                    if (textureDescription != null) {
                                        Texture texture = textureDescription.texture;
                                        if (ImGui.treeNode(texture.getTextureObjectHandle(), "Texture")) {
                                            ImGui.image(texture.getTextureObjectHandle(), ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailX(), textureAttribute.offsetU, textureAttribute.offsetV, textureAttribute.offsetU + textureAttribute.scaleU, textureAttribute.offsetV + textureAttribute.scaleV);
                                            ImGui.treePop();
                                        }
                                    }
                                } else if (attr instanceof ColorAttribute colorAttribute) {
                                    Color color = colorAttribute.color;

                                    float[] c = new float[4];
                                    c[0] = color.r;
                                    c[1] = color.g;
                                    c[2] = color.b;
                                    c[3] = color.a;
                                    if (ImGui.colorEdit4(field.getName(), c)) {
                                        try {
                                            field.set(component, color.set(c[0], c[1], c[2], c[3]));
                                        } catch (IllegalAccessException e) {
                                            // ignore
                                        }
                                    }
                                } else if (attr instanceof IntAttribute colorAttribute) {
                                    int value = colorAttribute.value;

                                    ImInt imInt = new ImInt(value);
                                    if (ImGui.inputInt(field.getName(), imInt)) {
                                        colorAttribute.value = imInt.get();
                                    }
                                } else if (attr instanceof FloatAttribute floatAttribute) {
                                    float value1 = floatAttribute.value;

                                    ImFloat imFloat = new ImFloat(value1);
                                    if (ImGui.inputFloat(field.getName(), imFloat)) {
                                        floatAttribute.value = imFloat.get();
                                    }
                                } else {
                                    ImGui.textColored(1, .5f, .5f, 1, "Unknown Attribute");
                                }
                            }
                        }
                        ImGui.endTable();
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof String s) {
                return () -> {
                    ImString ims = new ImString(s);
                    if (ImGui.inputText(field.getName(), ims, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                        try {
                            field.set(component, ims.get());
                        } catch (IllegalAccessException e) {
                            // ignore
                        }
                    }
                };
            } else if (object instanceof NamespaceID namespaceID) {
                return () -> {
                    ImString s = new ImString(namespaceID.getDomain());
                    ImString n = new ImString(namespaceID.getPath());

                    try {
                        ImGui.sameLine();
                        ImGui.setNextItemWidth((ImGui.getWindowSizeX() - ImGui.getCursorPosX()) / 2 - 5);

                        if (ImGui.inputText(field.getName(), s, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                            field.set(component, new NamespaceID(s.get(), n.get()));
                        }
                        ImGui.sameLine();
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - ImGui.getCursorPosX() - 5);
                        if (ImGui.inputText(" : ", n, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                            field.set(component, new NamespaceID(s.get(), n.get()));
                        }
                    } catch (Exception e) {
                        CommonConstants.LOGGER.error("Unable to set namespace id", e);
                    }
                };
            } else if (object instanceof Class<?> clazz) {
                for (Field field1 : clazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(field1.getModifiers()) || field1.isSynthetic()) continue;
                    if (field1.getType().equals(clazz)) {
                        return renderObject(null, field1, readOnly);
                    }
                }
                return null;
            } else if (object instanceof Enum<?>) {
                return () -> {
                    if (!readOnly) {
                        if (ImGui.beginCombo(field.getName(), object.toString())) {
                            //noinspection rawtypes
                            for (Object enumValue : EnumSet.allOf((Class<? extends Enum>) field.getType())) {
                                if (ImGui.selectable(enumValue.toString(), object.equals(enumValue))) {
                                    try {
                                        field.set(component, enumValue);
                                    } catch (IllegalAccessException e) {
                                        // ignore
                                    }
                                }
                            }

                            ImGui.endCombo();
                        }
                    } else {
                        ImGui.text(object.toString());
                    }
                };
            } else if (object instanceof Color color) {
                return () -> {
                    float[] c = new float[4];
                    c[0] = color.r;
                    c[1] = color.g;
                    c[2] = color.b;
                    c[3] = color.a;
                    if (ImGui.colorEdit4(field.getName(), c)) {
                        try {
                            field.set(component, color.set(c[0], c[1], c[2], c[3]));
                        } catch (IllegalAccessException e) {
                            // ignore
                        }
                    }
                };
            } else if (object instanceof Vector3 vec3) {
                return () -> {
                    float[] v = new float[3];
                    v[0] = vec3.x;
                    v[1] = vec3.y;
                    v[2] = vec3.z;
                    if (ImGui.inputFloat3(field.getName(), v)) {
                        try {
                            field.set(component, vec3.set(v[0], v[1], v[2]));
                        } catch (IllegalAccessException e) {
                            // ignore
                        }
                    }
                };
            } else if (object instanceof Vector2 vec3) {
                return () -> {
                    float[] v = new float[2];
                    v[0] = vec3.x;
                    v[1] = vec3.y;
                    if (ImGui.inputFloat2(field.getName(), v)) {
                        try {
                            field.set(component, vec3.set(v[0], v[1]));
                        } catch (IllegalAccessException e) {
                            // ignore
                        }
                    }
                };
            } else if (object instanceof Vector4 vec4) {
                return () -> {
                    float[] v = new float[4];
                    v[0] = vec4.x;
                    v[1] = vec4.y;
                    v[2] = vec4.z;
                    v[3] = vec4.w;
                    if (ImGui.inputFloat4(field.getName(), v)) {
                        try {
                            field.set(component, vec4.set(v[0], v[1], v[2], v[3]));
                        } catch (IllegalAccessException e) {
                            // ignore
                        }
                    }
                };
            } else if (object instanceof UUID uuid) {
                return () -> {
                    ImString text = new ImString(uuid.toString());
                    if (ImGui.inputText(field.getName(), text, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                        try {
                            field.set(component, UUID.fromString(text.get()));
                        } catch (IllegalArgumentException | IllegalAccessException ignored) {
                            // ignore
                        }
                    }
                };
            } else if (object instanceof GameNode gameObject) {
                return () -> {
                    if (ImGui.treeNode(System.identityHashCode(gameObject), gameObject.getName() == null ? gameObject.toString() : gameObject.getName())) {
                        renderComponent(gameObject);
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof List<?> list) {
                return () -> {
                    ImInt selected = new ImInt(-1);
                    List<String> result = new ArrayList<>();
                    for (Object o : list) {
                        String string = o.toString();
                        result.add(string);
                    }
                    ImGui.listBox("##List" + field.hashCode(), selected, result.toArray(new String[0]));
                    ImGui.sameLine(200);
                    ImGui.setNextItemWidth(ImGui.getWindowSizeX() - ImGui.getCursorPosX() - 110);
                    if (ImGui.treeNode(field.getName())) {
                        if (selected.get() >= 0 && selected.get() < list.size()) {
                            renderComponent(list.get(selected.get()));
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Map<?, ?> map) {
                return () -> {
                    if (ImGui.treeNode(field.getName())) {
                        if (ImGui.beginTable("##Map" + field.hashCode(), 2, ImGuiTableFlags.Borders)) {
                            ImGui.tableHeadersRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text("Key");
                            ImGui.tableSetColumnIndex(1);
                            ImGui.text("Value");
                            for (Map.Entry<?, ?> entry : map.entrySet()) {
                                ImGui.tableNextRow();
                                ImGui.tableSetColumnIndex(0);
                                ImGui.text(entry.getKey().toString());
                                ImGui.tableSetColumnIndex(1);
                                renderComponent(entry.getValue());
                            }
                            ImGui.endTable();
                        }
                        ImGui.treePop();
                    } else {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        ImGui.text(map.toString());
                    }
                };
            } else if (object instanceof Map.Entry<?, ?> entry) {
                return () -> {
                    ImGui.setNextItemWidth((ImGui.getWindowSizeX() - 200) / 2 - 5);
                    ImGui.text(entry.getKey().toString());
                    ImGui.sameLine((ImGui.getWindowSizeX() - 200) / 2 + 200);
                    ImGui.setNextItemWidth((ImGui.getWindowSizeX() - 200) / 2 - 5);
                    renderComponent(entry.getValue());
                };
            } else if (object instanceof AtomicReference<?> reference) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        renderComponent(reference.get());
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof AtomicBoolean atomicBoolean) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        ImBoolean b = new ImBoolean(atomicBoolean.get());
                        if (ImGui.checkbox(field.getName(), b)) {
                            atomicBoolean.set(b.get());
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof AtomicLong atomicLong) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        ImString text = new ImString(atomicLong.get() + "");
                        if (ImGui.inputText(field.getName(), text)) {
                            try {
                                atomicLong.set(Long.parseLong(text.get()));
                            } catch (NumberFormatException ignored) {

                            }
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof AtomicInteger atomicInteger) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        ImInt i = new ImInt(atomicInteger.get());
                        if (ImGui.inputInt(field.getName(), i)) {
                            atomicInteger.set(i.get());
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof BlockVec vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputInt3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof ChunkVec vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputInt3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof RegionVec vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputInt3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Vec3i vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputInt3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Vec3f vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        float[] i = new float[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputFloat3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Vec2i vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y};
                        if (ImGui.inputInt2(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Vec2f vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        float[] i = new float[]{vec.x, vec.y};
                        if (ImGui.inputFloat2(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Vec4i vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z, vec.w};
                        if (ImGui.inputInt4(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2], i[3]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Vec4f vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        float[] i = new float[]{vec.x, vec.y, vec.z, vec.w};
                        if (ImGui.inputFloat4(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2], i[3]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof BlockState state) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.text("ID: " + state.getBlock().getId());

                        if (ImGui.beginTable("##BlockState[" + System.identityHashCode(state), 2, ImGuiTableFlags.Borders)) {
                            ImGui.tableHeadersRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text("Key");
                            ImGui.tableSetColumnIndex(1);
                            ImGui.text("Value");

                            ImGui.tableNextRow();
                            for (StatePropertyKey<?> key : state.getBlock().getDefinition().getKeys()) {
                                ImGui.tableSetColumnIndex(0);
                                ImGui.text(key.getName());
                                ImGui.tableSetColumnIndex(1);
                                ImGui.text(String.valueOf(state.get(state.getDefinition().keyByName(key.getName()))));
                            }
                        }
                        ImGui.endTable();

                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Quaternion quat) {
                return () -> {
                    ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                    ImGui.combo("Rotation Type", rotType, "Euler\0Quaternion\0");
                    ImGui.sameLine(180);

                    if (rotType.get() == 0) {
                        if (ImGui.treeNode(field.hashCode(), field.getName())) {
                            ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                            float[] i = new float[]{quat.getYaw(), quat.getPitch(), quat.getRoll()};
                            if (ImGui.inputFloat3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                                quat.setEulerAngles(i[0], i[1], i[2]);
                            }
                            ImGui.treePop();
                        }
                    } else if (rotType.get() == 1) {
                        if (ImGui.treeNode(field.hashCode(), field.getName())) {
                            ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                            float[] i = new float[]{quat.x, quat.y, quat.z, quat.w};
                            if (ImGui.inputFloat4(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                                quat.set(i[0], i[1], i[2], i[3]);
                            }
                            ImGui.treePop();
                        }
                    }
                };
            }
            if (isAnnotationPresent(field.getType(), ShowInNodeView.class) && ImGui.treeNode(field.hashCode(), field.getName())) {
                return () -> {
                    renderComponent(object);
                    ImGui.treePop();
                };
            } else {
                return null;
            }
        } catch (Throwable e) {
            ImGui.textColored(1f, 0.5f, 0.5f, 1f, e.getMessage());
        }

        return null;
    }

    @Contract(pure = true)
    private static <T extends Annotation> boolean isAnnotationPresent(@NotNull Class<?> type, Class<T> anno) {
        if (type.isAnnotationPresent(anno)) {
            return true;
        }
//        while (type != Object.class) {
//
//            type = type.getSuperclass();
//        }

        return false;
    }

    private static void num(@Nullable Object component, Field field, boolean readOnly, Number number, Object object) {
        try {
            if (Objects.requireNonNull(number) instanceof Integer) {
                ImInt i = new ImInt((int) object);
                if (!readOnly) {
                    if (ImGui.inputInt(field.getName(), i, 1, 5000, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                        field.set(component, i.get());
                    }
                } else {
                    ImGui.text(String.valueOf(object));
                }
            } else if (number instanceof Float) {
                ImFloat f = new ImFloat((float) object);
                if (!readOnly) {
                    if (ImGui.inputFloat(field.getName(), f, 0.001f, 1, "%.3f", readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                        field.set(component, f.get());
                    }
                } else {
                    ImGui.text(String.valueOf(object));
                }
            } else if (number instanceof Double) {
                ImDouble d = new ImDouble((double) object);
                if (!readOnly) {
                    if (ImGui.inputDouble(field.getName(), d, 0.001, 1, "%.3f", readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                        field.set(component, d.get());
                    }
                } else {
                    ImGui.text(String.valueOf(object));
                }
            } else if (number instanceof Long) {
                ImString l = new ImString(String.valueOf(object));
                if (!readOnly) {
                    if (ImGui.inputText(field.getName(), l, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                        try {
                            field.set(component, Long.parseLong(l.get()));
                        } catch (NumberFormatException ignored) {

                        }
                    }
                } else {
                    ImGui.text(String.valueOf(object));
                }
            } else if (number instanceof Short) {
                ImInt s = new ImInt((int) object);
                if (!readOnly) {
                    if (ImGui.inputInt(field.getName(), s, 1, 5000, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                        field.set(component, (short) s.get());
                    }
                } else {
                    ImGui.text(String.valueOf(object));
                }
            } else if (number instanceof Byte) {
                ImInt b = new ImInt((int) object);
                if (!readOnly) {
                    if (ImGui.inputInt(field.getName(), b, 1, 20, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                        field.set(component, (byte) b.get());
                    }
                } else {
                    ImGui.text(String.valueOf(object));
                }
            } else {
                ImGui.text(String.valueOf(object));
            }
        } catch (IllegalAccessException e) {
            ImGui.textColored(1f, 0.5f, 0.5f, 1f, e.getMessage());
        }
    }

    private static void showSceneView() {
        if (ImGui.begin("Scene View", ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoMove)) {
            // Recursively render the scene view
            if (ImGui.treeNode(System.identityHashCode(QuantumClient.get().backgroundCat), "Background")) {
                renderGameNode(QuantumClient.get().backgroundCat);
                ImGui.treePop();
            }

            if (ImGui.treeNode(System.identityHashCode(QuantumClient.get().worldCat), "World")) {
                renderGameNode(QuantumClient.get().worldCat);
                ImGui.treePop();
            }

            if (ImGui.treeNode(System.identityHashCode(QuantumClient.get().mainCat), "Main")) {
                renderGameNode(QuantumClient.get().mainCat);
                ImGui.treePop();
            }
            if (ImGui.treeNode(System.identityHashCode(QuantumClient.get().profiler), "Profiler")) {
                QuantumClient.get().profiler.setProfiling(true);
                renderProfiler(QuantumClient.get().profiler);
                ImGui.treePop();
            } else {
                QuantumClient.get().profiler.setProfiling(false);
            }


            if (ImGui.treeNode(1, "Foreground")) {
                if (QuantumClient.get().screen != null) {
                    renderUINode(QuantumClient.get().screen);
                }
                ImGui.treePop();
            }

            if (ImGui.treeNode(2, "Selected Class")) {
                if (selectedClass != null) {
                    renderClass(selectedClass);
                }
                ImGui.treePop();
            }

            ImGui.getWindowSizeX();
        } else {
            QuantumClient.get().profiler.setProfiling(false);
        }
        ImGui.end();
    }

    private static void renderProfiler(Profiler profiler) {
        if (nextProfilerCollect < System.currentTimeMillis()) {
            profilerData = profiler.collect();
            List<Thread> list = new ArrayList<>();
            for (Thread thread : profilerData.getThreads()) {
                list.add(thread);
            }
            list.sort(Comparator.comparing(Thread::getName));
            threads = list;
            nextProfilerCollect = System.currentTimeMillis() + 1000;
        }
        for (Thread thread : threads) {
            if (ImGui.treeNode("ProfilerThread." + thread.getId(), thread.getName())) {
                ThreadSection.FinishedThreadSection threadSection = profilerData.getThreadSection(thread);
                if (threadSection != null) {
                    extracted(thread, threadSection);
                }
                ImGui.treePop();
            }
        }
    }

    private static void extracted(Thread thread, ThreadSection.FinishedThreadSection threadSection) {
        for (Map.Entry<String, Section.FinishedSection> section : threadSection.getData().entrySet()) {
            String strId = "ProfilerThread." + thread.getId() + "/" + section.getKey();
            if (ImGui.treeNode(strId, section.getKey() + " (" + section.getValue().getNanos() + "ms)")) {
                for (Map.Entry<String, Integer> info : section.getValue().getStats().entrySet()) {
                    ImGuiEx.editInt(info.getKey(), strId + ":" + info.getKey(), info::getValue, v -> {});
                }
                Section.FinishedSection finishedSection = section.getValue();
                extracted(strId, thread, finishedSection);
                ImGui.treePop();
            }
        }
    }

    private static void extracted(String parentId, Thread thread, Section.FinishedSection threadSection) {
        for (Map.Entry<String, Section.FinishedSection> section : threadSection.getData().entrySet()) {
            String strId = parentId + "/" + section.getKey();
            if (ImGui.treeNode(strId, section.getKey() + " (" + section.getValue().getNanos() / 1000000L + "ms)")) {
                for (Map.Entry<String, Integer> info : section.getValue().getStats().entrySet()) {
                    ImGuiEx.editInt(info.getKey(), strId + ":" + info.getKey(), info::getValue, v -> {});
                }
                Section.FinishedSection finishedSection = section.getValue();
                extracted(strId, thread, finishedSection);
                ImGui.treePop();
            }
        }
    }

    private static final ImVec2 rectMin = new ImVec2();

    private static final ImVec2 rectMax = new ImVec2();
    private static final ImVec2 mousePos = new ImVec2();
    private static void renderClass(Class<?> selectedClass) {
        for (Field field : selectedClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;

            if (field.isAnnotationPresent(ShowInNodeView.class) && !field.isAnnotationPresent(HideInNodeView.class) || SHOW_HIDDEN_FIELDS.get()) {
                ImGui.pushID(field.hashCode());
                try {
                    field.setAccessible(true);
                    Object object = field.get(QuantumClient.get().screen);
                    if (object != null) {
                        if (ImGui.treeNode(field.hashCode(), field.getName())) {
                            try {
                                renderObject(object);
                            } finally {
                                ImGui.treePop();
                            }
                        }
                        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
                            selected = object;
                        }
                    }
                } catch (Throwable e) {
                    ImGui.textColored(1f, 0.5f, 0.5f, 1f, e.getMessage());
                } finally {
                    ImGui.popID();
                }
            }
        }
    }

    private static void renderObject(Object object) {
        for (Field field : object.getClass().getFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;

            if (field.isAnnotationPresent(ShowInNodeView.class) && !field.isAnnotationPresent(HideInNodeView.class) || SHOW_HIDDEN_FIELDS.get()) {
                ImGui.pushID(field.hashCode());
                try {
                    field.setAccessible(true);
                    if (ImGui.treeNode(field.hashCode(), field.getName() + " (" + field.getType().getSimpleName() + ".class)")) {
                        try {
                            Object component = field.get(object);
                            if (component != null) {
                                renderObject(component);
                            }
                        } finally {
                            ImGui.treePop();
                        }
                    }
                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.textColored(1f, 1f, 0f, 1f, field.getName());
                        try {
                            field.setAccessible(true);
                            Object component = field.get(object);
                            if (component != null) {
                                ImGui.text(component.getClass().getSimpleName());
                                ImGui.text("");
                                ImGui.textColored(.5f, .5f, .5f, 1f, String.valueOf(component));
                            }
                        } catch (Throwable ignored) {
                            ImGui.textColored(.5f, .5f, .5f, 1f, "null");
                        }
                        ImGui.endTooltip();
                    }
                    if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
                        selected = object;
                    }
                } catch (Throwable e) {
                    ImGui.textColored(1f, 0.5f, 0.5f, 1f, e.getMessage());
                } finally {
                    ImGui.popID();
                }
            }
        }
    }

    private static void renderGameNode(GameNode object) {
        for (GameNode child : object.getChildren()) {
            if (ImGui.treeNodeEx(System.identityHashCode(child), selected == child ? ImGuiTreeNodeFlags.Selected : ImGuiTreeNodeFlags.OpenOnArrow, child.getName() == null ? child.toString() : child.getName())) {
                ImGui.getItemRectMin(rectMin);
                ImGui.getItemRectMax(rectMax);
                ImGui.getMousePos(mousePos);
                if (ImGui.isMouseClicked(ImGuiMouseButton.Right) || ImGui.isMouseClicked(ImGuiMouseButton.Left))
                    if (ImGui.isMouseHoveringRect(rectMin, rectMax)) {
                        selected = child;
                    }
                if (ImGui.isItemHovered() && child.getDescription() != null)
                    ImGui.setTooltip(child.getDescription());

                renderGameNode(child);

                ImGui.treePop();
            } else {
                ImGui.getItemRectMin(rectMin);
                ImGui.getItemRectMax(rectMax);
                ImGui.getMousePos(mousePos);
                if (ImGui.isMouseClicked(ImGuiMouseButton.Right) || ImGui.isMouseClicked(ImGuiMouseButton.Left))
                    if (ImGui.isMouseHoveringRect(rectMin, rectMax)) {
                        selected = child;
                    }
                if (ImGui.isItemHovered() && child.getDescription() != null)
                    ImGui.setTooltip(child.getDescription());
            }
        }
    }

    private static void renderUINode(Widget widget) {
        if (widget instanceof UIContainer<?> container) {
            for (Widget child : container.children()) {
                if (ImGui.treeNodeEx(System.identityHashCode(child), selected == child ? ImGuiTreeNodeFlags.Selected : ImGuiTreeNodeFlags.OpenOnArrow, child.toString())) {
                    renderUINode(child);

                    ImGui.treePop();
                }
            }
        }
    }

    private static void showModelViewer() {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Model Viewer", ImGuiOverlay.getDefaultFlags())) {
            if (ImGui.button("Reload")) {
                List<String> list = new ArrayList<>();
                for (EntityType<?> entityType : QuantumClient.get().entityModelManager.getRegistry().keySet()) {
                    NamespaceID id = entityType.getId();
                    String string = Objects.toString(id);
                    list.add(string);
                }
                list.sort(String.CASE_INSENSITIVE_ORDER);
                modelViewerList = list.toArray(new String[0]);
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
                ImGui.separator();
                if (ImGui.menuItem("Hook Game Crash", "", crashHooked, !crashHooked)) {
                    crashHooked = true;
                    QuantumClient.setCrashHook(caller -> {
                        CRASH_HOOK.accept(caller.getThrowable());
                    });
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("View")) {
                ImGui.menuItem("Utils", null, ImGuiOverlay.SHOW_UTILS);
                ImGui.separator();
                ImGui.menuItem("Chunks", null, ImGuiOverlay.SHOW_CHUNK_DEBUGGER);
                ImGui.menuItem("Chunk Node Borders", "Ctrl+F4", ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS);
                ImGui.separator();
                ImGui.menuItem("InspectionRoot", "Ctrl+P", ImGuiOverlay.SHOW_PROFILER);
                ImGui.menuItem("Render Pipeline", null, ImGuiOverlay.SHOW_RENDER_PIPELINE);
                ImGui.menuItem("Model Viewer", null, ImGuiOverlay.SHOW_MODEL_VIEWER);
                ImGui.menuItem("Network Logging", null, ImGuiOverlay.SHOW_NETWORK_LOGGING);
                ImGui.separator();
                ImGui.menuItem("Show Hidden Fields", null, SHOW_HIDDEN_FIELDS);
                ImGui.menuItem("Show Occlusion Debug", null, SHOW_OCCLUSION_DEBUG);
                ImGui.separator();
                ImGui.menuItem("Classes", null, SHOW_CLASS_ATTACHER);
                ImGui.menuItem("JShell", null, SHOW_JSHELL);
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Help")) {
                ImGui.menuItem("About", null, ImGuiOverlay.SHOW_ABOUT);
                ImGui.separator();

                ImGui.menuItem("Metrics", null, ImGuiOverlay.SHOW_METRICS);
                ImGui.menuItem("Stack Tool", null, ImGuiOverlay.SHOW_STACK_TOOL);
                ImGui.menuItem("Style Editor", null, ImGuiOverlay.SHOW_STYLE_EDITOR);
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Gizmos")) {
                @Nullable ClientWorldAccess terrainRenderer = QuantumClient.get().world;
                if (terrainRenderer instanceof ClientWorld world) {
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
            ImGui.text(" Client TPS: " + QuantumClient.get().getCurrentTps() + " ");
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

    private static void showShaderEditor() {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Shader Editor", ImGuiOverlay.getDefaultFlags())) {
            if (ImGui.treeNode("Shader::SSAO", "SSAO")) {
                ImGuiEx.editFloat("iGamma", "Shader::SSAO::iGamma", ImGuiOverlay.I_GAMMA::get, ImGuiOverlay.I_GAMMA::set);
                ImGui.treePop();
            }
            if (ImGui.treeNode("Shader::Debug", "Debugging")) {
                ImGuiEx.editInt("State", "Shader::Debug::State", ImGuiOverlay.SHADER_DEBUG_STATE::get, ImGuiOverlay.SHADER_DEBUG_STATE::set);
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
                ImGuiEx.editDouble("FogDensity", "Shader::World::FogDensity", () -> ClientWorld.FOG_DENSITY, v -> ClientWorld.FOG_DENSITY = v);
                ImGuiEx.editDouble("FogStart", "Shader::World::FogStart", () -> ClientWorld.FOG_START, v -> ClientWorld.FOG_START = v);
                ImGuiEx.editDouble("FogEnd", "Shader::World::FogEnd", () -> ClientWorld.FOG_END, v -> ClientWorld.FOG_END = v);
                ImGuiEx.editVec2f("AtlasSize", "Shader::World::AtlasSize", ClientWorld.ATLAS_SIZE::get, ClientWorld.ATLAS_SIZE::set);
                ImGuiEx.editVec3f("CameraUp", "Shader::World::CameraUp", () -> new Vec3f(WorldShader.CAMERA_UP.x, WorldShader.CAMERA_UP.y, WorldShader.CAMERA_UP.z), vec3f -> WorldShader.CAMERA_UP.set(vec3f.x, vec3f.y, vec3f.z));
                ImGui.treePop();
            }
        }

        ImGui.end();
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
        }

        ImGui.end();
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
            ImGuiEx.editBool("Allow Flight:", "PlayerAllowFlight", client.player::isAllowFlight, v -> {
            });
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
        }

        ImGui.end();
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
        int flags = ImGuiWindowFlags.None;
        if (cursorCaught) flags |= ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoInputs;
        return flags;
    }

    public static boolean isShown() {
        return ImGuiOverlay.SHOW_IM_GUI.get() && GamePlatform.get().isDesktop();
    }

    public static void setShowingImGui(boolean value) {
        ImGuiOverlay.SHOW_IM_GUI.set(value);
    }

    public static boolean isProfilerShown() {
        return ImGuiOverlay.SHOW_PROFILER.get();
    }

    public static void dispose() {
        if (GamePlatform.get().isAngleGLES()) {
            LoggerFactory.getLogger(ImGuiOverlay.class).trace("ImGui Disabled for Angle GLES");
            return;
        }

        synchronized (ImGuiOverlay.class) {
            if (ImGuiOverlay.isImplCreated) {
                ImGuiOverlay.imGuiGl3.shutdown();
                ImGuiOverlay.imGuiGlfw.shutdown();
                ImGuiOverlay.isImplCreated = false;
            }

            if (ImGuiOverlay.isContextCreated) {
                ImGui.destroyContext();
                ImPlot.destroyContext(ImGuiOverlay.imPlotCtx);
                ImGuiOverlay.isContextCreated = false;
            }
        }
    }

    private static void setSkyboxRot(float v) {
        ClientWorld.SKYBOX_ROTATION = Rot.deg(v);
    }

    public static void setBounds(GameInsets bounds) {
        bounds.set(ImGuiOverlay.bounds);
    }

    public static boolean isDevFlagEnabled(DevFlag devFlag) {
        return switch (devFlag) {
            case ShowChunkSectionBorders -> ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.get();
            case ShowChunkDebugger -> ImGuiOverlay.SHOW_CHUNK_DEBUGGER.get();
            case ShowMetrics -> ImGuiOverlay.SHOW_METRICS.get();
            case ShowModelViewer -> ImGuiOverlay.SHOW_MODEL_VIEWER.get();
            case ShowProfiler -> ImGuiOverlay.SHOW_PROFILER.get();
            case OcclusionDebug -> ImGuiOverlay.SHOW_OCCLUSION_DEBUG.get();
            case NetworkLogging -> ImGuiOverlay.SHOW_NETWORK_LOGGING.get();
            default -> false;
        };
    }
}
