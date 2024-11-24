package dev.ultreon.quantum.desktop.imgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.google.common.util.concurrent.AtomicDouble;
import dev.ultreon.quantum.GameInsets;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.block.state.StatePropertyKey;
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
import dev.ultreon.quantum.desktop.DesktopLauncher;
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
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

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

    private static final ImBoolean SHOW_ABOUT = new ImBoolean(false);
    private static final ImBoolean SHOW_METRICS = new ImBoolean(false);
    private static final ImBoolean SHOW_STACK_TOOL = new ImBoolean(false);
    private static final ImBoolean SHOW_STYLE_EDITOR = new ImBoolean(false);

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
    private static GameObject selected = null;
    private static final GameInsets bounds = new GameInsets();
    private static final ImInt rotType = new ImInt(0);
    private static TextureRegion frameBufferPixels;
    private static final Map<NamespaceID, TextEditor> textEditors = new HashMap<>();
    private static TextEditorLanguageDefinition glsl;
    private static final Map<NamespaceID, TextEditorCoordinates> textEditorPos = new HashMap<>();

    public static void setupImGui() {
        if (GamePlatform.get().isAngleGLES()) return;

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

        long windowHandle = DesktopLauncher.getGameWindow().getHandle();

        QuantumClient.invokeAndWait(() -> {
            ImGuiOverlay.imGuiGlfw.init(windowHandle, true);
            ImGuiOverlay.imGuiGl3.init("#version 140");

            glsl = TextEditorLanguageDefinition.GLSL();
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
        if (frameBufferPixels != null) frameBufferPixels.getTexture().dispose();
        frameBufferPixels = ScreenUtils.getFrameBufferTexture(0, Gdx.graphics.getHeight() - bounds.height, bounds.width, bounds.height);

        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);

        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX(), ImGui.getMainViewport().getPosY() + 18);
        ImGui.setNextWindowSize(ImGui.getMainViewport().getSizeX(), ImGui.getMainViewport().getSizeY() - 18);
        ImGui.setNextWindowCollapsed(false);

        ImGui.getStyle().setWindowPadding(0, 0);
        ImGui.getStyle().setWindowBorderSize(0);

        ImGui.begin("MainDockingArea", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoScrollbar);
        int id = ImGui.getID("MainDockingArea");
        ImGui.dockSpace(id);
        ImGui.end();

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
    }

    private static void showGame(QuantumClient client) {
        if (ImGui.begin("Game", ImGuiWindowFlags.AlwaysAutoResize)) {
            bounds.left = (int) ((ImGui.getMousePosX() - ImGui.getCursorPosX() - ImGui.getWindowPosX()) * ImGui.getWindowDpiScale());
            bounds.top = (int) ((ImGui.getMousePosY() - ImGui.getCursorPosY() - ImGui.getWindowPosY()) * ImGui.getWindowDpiScale());
            float contentRegionAvailX = ImGui.getContentRegionAvailX();
            float contentRegionAvailY = ImGui.getContentRegionAvailY();
            bounds.width = (int) (contentRegionAvailX * ImGui.getWindowDpiScale());
            bounds.height = (int) (contentRegionAvailY * ImGui.getWindowDpiScale());

            TextureRegion gameTex = frameBufferPixels;
            ImGui.image(gameTex.getTexture().getTextureObjectHandle(), contentRegionAvailX, contentRegionAvailY, frameBufferPixels.getU(), frameBufferPixels.getV(), frameBufferPixels.getU2(), frameBufferPixels.getV2(), 1, 1, 1, 1);

            ImGui.setCursorPos(50, 50);

            if (ImGui.beginChild("GameOverlay", 200, 200, true)) {
                ImGui.text("Insets: " + bounds.left + ", " + bounds.top + ", " + bounds.width + ", " + bounds.height);
                ImGui.text("Pos: " + ImGui.getWindowPosX() + ", " + ImGui.getWindowPosY());
                ImGui.text("Size: " + ImGui.getWindowSizeX() + ", " + ImGui.getWindowSizeY());
                ImGui.text("Dpi: " + ImGui.getWindowDpiScale());
                ImGui.text("Viewport Pos: " + ImGui.getWindowViewport().getPosX() + ", " + ImGui.getWindowViewport().getPosY());
                ImGui.text("Viewport Size: " + ImGui.getWindowViewport().getSizeX() + ", " + ImGui.getWindowViewport().getSizeY());
                ImGui.text("Viewport Dpi: " + ImGui.getWindowViewport().getDpiScale());
                ImGui.text("Mouse Pos: " + ImGui.getMousePosX() + ", " + ImGui.getMousePosY());
            }
            ImGui.endChild();
        }
        ImGui.end();
    }

    private static void showAssetView(QuantumClient client) {
        if (ImGui.begin("Asset View")) {
            // Show a list of all assets
            ResourceManager resourceManager = client.getResourceManager();
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

                                        float v = textEditor.getTotalLines() * (ImGui.getFont().getFontSize()) + 16;
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

    private static void showNodeView(QuantumClient client) {
        if (ImGui.begin("Node View")) {
            GameObject sel = selected;

            if (sel != null) {
                for (Component<?> component : sel.getComponents()) {
                    if (ImGui.treeNode(component.getClass().getName(), component instanceof GameComponent ? component.getClass().getSimpleName() : component.getClass().getSimpleName() + (GamePlatform.get().isDevEnvironment() ? ".java" : ".class"))) {
                        renderComponent(component);
                        ImGui.treePop();
                    }
                }

                if (ImGui.treeNode(sel.getClass().getName(), sel.getClass().getSimpleName() + (GamePlatform.get().isDevEnvironment() ? ".java" : ".class"))) {
                    renderComponent(sel);
                    ImGui.treePop();
                }
            }
        }
        ImGui.end();
    }

    private static void renderComponent(final @Nullable Object component) {
        if (component == null) return;
        if (ImGui.beginTable("##<<Comp>> " + System.identityHashCode(component), 2, ImGuiTableFlags.Borders)) {
            ImGui.tableSetupColumn("Field", ImGuiTableColumnFlags.WidthFixed, 100, 0);
            ImGui.tableSetupColumn("Value", ImGuiTableColumnFlags.WidthStretch, 1);
            ImGui.tableHeadersRow();
            ImGui.tableSetColumnIndex(0);
            ImGui.text("Field");
            ImGui.tableSetColumnIndex(1);
            ImGui.text("Value");

            Class<?> clazz = component.getClass();
            while (clazz != Object.class) {
                for (Field field : component.getClass().getDeclaredFields()) {
                    if (field.getDeclaringClass() != clazz
                            || !Modifier.isPublic(field.getModifiers()) && !field.isAnnotationPresent(ShowInNodeView.class)
                            || Modifier.isStatic(field.getModifiers())
                            || field.isSynthetic()
                            || field.isAnnotationPresent(HiddenNode.class))
                        continue;

                    boolean readOnly = Modifier.isFinal(field.getModifiers());

                    ImGui.tableNextRow();
                    ImGui.tableSetColumnIndex(0);
                    ImGui.text(field.getName());
                    ImGui.tableSetColumnIndex(1);
                    if (renderObject(component, field, readOnly)) {
                        // TODO
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
        ImGui.endTable();
    }

    @SuppressWarnings("unchecked")
    private static boolean renderObject(Object component, Field field, boolean readOnly) {
        try {
            field.setAccessible(true);
            Object object = field.get(component);
            if (field.getType().isPrimitive()) {
                switch (object) {
                    case Number number -> {
                        num(component, field, readOnly, number, object);
                    }
                    case Boolean aBoolean -> {
                        ImBoolean b = new ImBoolean((boolean) object);
                        if (ImGui.checkbox(field.getName(), b) && !readOnly) {
                            field.set(component, b.get());
                        }
                        ImGui.sameLine(120);
                        ImGui.text((boolean) object ? "True" : "False");
                    }
                    case null, default -> ImGui.text(String.valueOf(object));
                }
                return true;
            }
            switch (object) {
                case GLTexture texture -> {
                    if (ImGui.treeNode(field.hashCode(), "Texture")) {
                        ImGui.image(texture.getTextureObjectHandle(), ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailX());
                        ImGui.treePop();
                    }
                }
                case TextureRegion region -> {
                    if (ImGui.treeNode(field.hashCode(), "Texture Region")) {
                        ImGui.image(region.getTexture().getTextureObjectHandle(), ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailX(), region.getU(), region.getV(), region.getU2(), region.getV2());
                        ImGui.treePop();
                    }
                }
                case Material material -> {
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

                                switch (attr) {
                                    case TextureAttribute textureAttribute -> {
                                        TextureDescriptor<Texture> textureDescription = textureAttribute.textureDescription;
                                        if (textureDescription != null) {
                                            Texture texture = textureDescription.texture;
                                            if (ImGui.treeNode(texture.getTextureObjectHandle(), "Texture")) {
                                                ImGui.image(texture.getTextureObjectHandle(), ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailX(), textureAttribute.offsetU, textureAttribute.offsetV, textureAttribute.offsetU + textureAttribute.scaleU, textureAttribute.offsetV + textureAttribute.scaleV);
                                                ImGui.treePop();
                                            }
                                        }
                                    }
                                    case ColorAttribute colorAttribute -> {
                                        Color color = colorAttribute.color;

                                        float[] c = new float[4];
                                        c[0] = color.r;
                                        c[1] = color.g;
                                        c[2] = color.b;
                                        c[3] = color.a;
                                        if (ImGui.colorEdit4(field.getName(), c)) {
                                            field.set(component, color.set(c[0], c[1], c[2], c[3]));
                                        }
                                    }
                                    case IntAttribute colorAttribute -> {
                                        int value = colorAttribute.value;

                                        ImInt imInt = new ImInt(value);
                                        if (ImGui.inputInt(field.getName(), imInt)) {
                                            colorAttribute.value = imInt.get();
                                        }
                                    }
                                    case FloatAttribute floatAttribute -> {
                                        float value = floatAttribute.value;

                                        ImFloat imFloat = new ImFloat(value);
                                        if (ImGui.inputFloat(field.getName(), imFloat)) {
                                            floatAttribute.value = imFloat.get();
                                        }
                                    }
                                    case null, default -> {
                                        ImGui.textColored(1, .5f, .5f, 1, "Unknown Attribute");
                                    }
                                }
                            }
                        }
                        ImGui.endTable();
                        ImGui.treePop();
                    }
                }
                case String s -> {
                    ImString ims = new ImString(s);
                    if (ImGui.inputText(field.getName(), ims, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                        field.set(component, ims.get());
                    }
                }
                case NamespaceID namespaceID -> {
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
                        e.printStackTrace();
                    }
                }
                case Enum anEnum -> {
                    if (!readOnly) {
                        if (ImGui.beginCombo(field.getName(), object.toString())) {
                            for (Object enumValue : EnumSet.allOf((Class<? extends Enum>) field.getType())) {
                                if (ImGui.selectable(enumValue.toString(), object.equals(enumValue))) {
                                    field.set(component, enumValue);
                                }
                            }
                        }
                    } else {
                        ImGui.text(object.toString());
                    }
                }
                case Color color -> {
                    float[] c = new float[4];
                    c[0] = color.r;
                    c[1] = color.g;
                    c[2] = color.b;
                    c[3] = color.a;
                    if (ImGui.colorEdit4(field.getName(), c)) {
                        field.set(component, color.set(c[0], c[1], c[2], c[3]));
                    }
                }
                case Vector3 vec3 -> {
                    float[] v = new float[3];
                    v[0] = vec3.x;
                    v[1] = vec3.y;
                    v[2] = vec3.z;
                    if (ImGui.inputFloat3(field.getName(), v)) {
                        field.set(component, vec3.set(v[0], v[1], v[2]));
                    }
                }
                case Vector2 vec3 -> {
                    float[] v = new float[2];
                    v[0] = vec3.x;
                    v[1] = vec3.y;
                    if (ImGui.inputFloat2(field.getName(), v)) {
                        field.set(component, vec3.set(v[0], v[1]));
                    }
                }
                case Vector4 vec4 -> {
                    float[] v = new float[4];
                    v[0] = vec4.x;
                    v[1] = vec4.y;
                    v[2] = vec4.z;
                    v[3] = vec4.w;
                    if (ImGui.inputFloat4(field.getName(), v)) {
                        field.set(component, vec4.set(v[0], v[1], v[2], v[3]));
                    }
                }
                case UUID uuid -> {
                    ImString text = new ImString(uuid.toString());
                    if (ImGui.inputText(field.getName(), text, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                        try {
                            field.set(component, UUID.fromString(text.get()));
                        } catch (IllegalArgumentException ignored) {

                        }
                    }
                }
                case GameObject gameObject -> {
                    if (ImGui.treeNode(System.identityHashCode(gameObject), gameObject.getName() == null ? gameObject.toString() : gameObject.getName())) {
                        renderComponent(gameObject);
                        ImGui.treePop();
                    }
                }
                case List<?> list -> {
                    ImInt selected = new ImInt(-1);
                    ImGui.listBox("##List" + field.hashCode(), selected, list.stream().map(Object::toString).toArray(String[]::new));
                    ImGui.sameLine(200);
                    ImGui.setNextItemWidth(ImGui.getWindowSizeX() - ImGui.getCursorPosX() - 110);
                    if (ImGui.treeNode(field.getName())) {
                        if (selected.get() >= 0 && selected.get() < list.size()) {
                            renderComponent(list.get(selected.get()));
                        }
                        ImGui.treePop();
                    }
                }
                case Map<?, ?> map -> {
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
                }
                case Map.Entry<?, ?> entry -> {
                    ImGui.setNextItemWidth((ImGui.getWindowSizeX() - 200) / 2 - 5);
                    ImGui.text(entry.getKey().toString());
                    ImGui.sameLine((ImGui.getWindowSizeX() - 200) / 2 + 200);
                    ImGui.setNextItemWidth((ImGui.getWindowSizeX() - 200) / 2 - 5);
                    renderComponent(entry.getValue());
                }
                case AtomicReference<?> reference -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        renderComponent(reference.get());
                        ImGui.treePop();
                    }
                }
                case AtomicBoolean atomicBoolean -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        ImBoolean b = new ImBoolean(atomicBoolean.get());
                        if (ImGui.checkbox(field.getName(), b)) {
                            atomicBoolean.set(b.get());
                        }
                        ImGui.treePop();
                    }
                }
                case AtomicLong atomicLong -> {
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
                }
                case AtomicInteger atomicInteger -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        ImInt i = new ImInt(atomicInteger.get());
                        if (ImGui.inputInt(field.getName(), i)) {
                            atomicInteger.set(i.get());
                        }
                        ImGui.treePop();
                    }
                }
                case AtomicDouble atomicDouble -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        ImDouble d = new ImDouble(atomicDouble.get());
                        if (ImGui.inputDouble(field.getName(), d)) {
                            atomicDouble.set(d.get());
                        }
                        ImGui.treePop();
                    }
                }
                case BlockVec vec -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputInt3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                }
                case ChunkVec vec -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputInt3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                }
                case RegionVec vec -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputInt3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                }
                case Vec3i vec -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputInt3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                }
                case Vec3f vec -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        float[] i = new float[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputFloat3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                }
                case Vec2i vec -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y};
                        if (ImGui.inputInt2(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1]);
                        }
                        ImGui.treePop();
                    }
                }
                case Vec2f vec -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        float[] i = new float[]{vec.x, vec.y};
                        if (ImGui.inputFloat2(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1]);
                        }
                        ImGui.treePop();
                    }
                }
                case Vec4i vec -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z, vec.w};
                        if (ImGui.inputInt4(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2], i[3]);
                        }
                        ImGui.treePop();
                    }
                }
                case Vec4f vec -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        float[] i = new float[]{vec.x, vec.y, vec.z, vec.w};
                        if (ImGui.inputFloat4(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2], i[3]);
                        }
                        ImGui.treePop();
                    }
                }
                case BlockState state -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.text("ID: " + state.getBlock().getId());

                        if (ImGui.beginTable("##BlockState[" + System.identityHashCode(state), 2, ImGuiTableFlags.Borders)) {
                            ImGui.tableHeadersRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text("Key");
                            ImGui.tableSetColumnIndex(1);
                            ImGui.text("Value");

                            ImGui.tableNextRow();
                            for (StatePropertyKey<?> key : state.getBlock().getDefinition().keys()) {
                                ImGui.tableSetColumnIndex(0);
                                ImGui.text(key.getName());
                                ImGui.tableSetColumnIndex(1);
                                ImGui.text(state.get(key.getName()));
                            }
                        }
                        ImGui.endTable();

                        ImGui.treePop();
                    }
                }
                case Quaternion quat -> {
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
                }
                case null, default -> {
                    if (isAnnotationPresent(field.getType(), ShowInNodeView.class) && ImGui.treeNode(field.hashCode(), field.getName())) {
                        renderComponent(object);
                        ImGui.treePop();
                    }
                }
            }
        } catch (IllegalAccessException e) {
            ImGui.textColored(1f, 0.5f, 0.5f, 1f, e.getMessage());
        }
        return readOnly;
    }

    private static <T extends Annotation> boolean isAnnotationPresent(Class<?> type, Class<T> anno) {
        if (type.isAnnotationPresent(anno)) {
            return true;
        }
//        while (type != Object.class) {
//
//            type = type.getSuperclass();
//        }

        return false;
    }

    private static void num(Object component, Field field, boolean readOnly, Number number, Object object) throws IllegalAccessException {
        switch (number) {
            case Integer integer -> {
                ImInt i = new ImInt((int) object);
                if (!readOnly) {
                    if (ImGui.inputInt(field.getName(), i, 1, 5000, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                        field.set(component, i.get());
                    }
                } else {
                    ImGui.text(String.valueOf(object));
                }
            }
            case Float v -> {
                ImFloat f = new ImFloat((float) object);
                if (!readOnly) {
                    if (ImGui.inputFloat(field.getName(), f, 0.001f, 1, "%.3f", readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                        field.set(component, f.get());
                    }
                } else {
                    ImGui.text(String.valueOf(object));
                }
            }
            case Double v -> {
                ImDouble d = new ImDouble((double) object);
                if (!readOnly) {
                    if (ImGui.inputDouble(field.getName(), d, 0.001, 1, "%.3f", readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                        field.set(component, d.get());
                    }
                } else {
                    ImGui.text(String.valueOf(object));
                }
            }
            case Long aLong -> {
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
            }
            case Short i -> {
                ImInt s = new ImInt((int) object);
                if (!readOnly) {
                    if (ImGui.inputInt(field.getName(), s, 1, 5000, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                        field.set(component, (short) s.get());
                    }
                } else {
                    ImGui.text(String.valueOf(object));
                }
            }
            case Byte aByte -> {
                ImInt b = new ImInt((int) object);
                if (!readOnly) {
                    if (ImGui.inputInt(field.getName(), b, 1, 20, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                        field.set(component, (byte) b.get());
                    }
                } else {
                    ImGui.text(String.valueOf(object));
                }
            }
            default -> ImGui.text(String.valueOf(object));
        }
    }

    private static void showSceneView() {
        if (ImGui.begin("Scene View", ImGuiWindowFlags.AlwaysAutoResize)) {
            // Recursively render the scene view
            if (ImGui.treeNode(System.identityHashCode(QuantumClient.get().backgroundCat), "Background")) {
                renderSceneNode(QuantumClient.get().backgroundCat);
                ImGui.treePop();
            }

            if (ImGui.treeNode(System.identityHashCode(QuantumClient.get().worldCat), "World")) {
                renderSceneNode(QuantumClient.get().worldCat);
                ImGui.treePop();
            }

            if (ImGui.treeNode(System.identityHashCode(QuantumClient.get().mainCat), "Main")) {
                renderSceneNode(QuantumClient.get().mainCat);
                ImGui.treePop();
            }

            if (ImGui.treeNode(1, "Foreground")) {
                if (QuantumClient.get().screen != null) {
                    renderUINode(QuantumClient.get().screen);
                }
                ImGui.treePop();
            }

            ImGui.getWindowSizeX();
        }
        ImGui.end();
    }

    private static void renderSceneNode(GameObject object) {
        for (GameObject child : object.getChildren()) {
            if (ImGui.treeNodeEx(System.identityHashCode(child), selected == child ? ImGuiTreeNodeFlags.Selected : ImGuiTreeNodeFlags.OpenOnArrow, child.getName() == null ? child.toString() : child.getName())) {
                renderSceneNode(child);
                ImGui.treePop();
            }

            if (ImGui.isItemHovered() && child.getDescription() != null) {
                ImGui.setTooltip(child.getDescription());
            }

            if (ImGui.isItemClicked()) {
                selected = child;
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
}
