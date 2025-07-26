package dev.ultreon;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import dev.ultreon.win32.Dwmapi;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.io.InputStream;

public class PermissionHelper {
    private static String type;

    private static void setupVibrancy(long handle) {
        // Check for OS and apply acrylic/mica/vibrancy
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            WinDef.HWND hwnd = new WinDef.HWND(new Pointer(GLFWNativeWin32.glfwGetWin32Window(handle)));
            Dwmapi.setAcrylicBackground(hwnd);
            Dwmapi.setUseImmersiveDarkMode(hwnd, true);

            Dwmapi.removeBorder(hwnd);
        }
    }


    public static void main(String[] args) {
        if (args.length != 2 && args.length != 3) {
            System.err.println("Usage: PermissionHelper <title> <message> [<type>]");
            System.exit(1);
            return;
        }

        String title = args[0];
        String message = args[1];
        type = "Continue,Cancel";
        if (args.length == 3) {
            type = args[2];
        }

        if (title == null || message == null) {
            System.err.println("Usage: PermissionHelper <title> <message>");
            System.exit(-1);
            return;
        }

        if (!GLFW.glfwInit()) {
            System.err.println("Failed to initialize GLFW!");
            System.exit(-1);
            return;
        }

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE);

        long window = GLFW.glfwCreateWindow(480, 120, title, 0, 0);
        if (window == 0) {
            System.err.println("Failed to create window!");
            System.exit(-1);
            return;
        }

        setupVibrancy(window);

        GLFW.glfwSetWindowTitle(window, title);
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwShowWindow(window);

        GL.createCapabilities();

        ImGuiImplGlfw glfw = new ImGuiImplGlfw();
        ImGuiImplGl3 gl3 = new ImGuiImplGl3();

        ImGui.createContext();


        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);

        try (InputStream resourceAsStream = PermissionHelper.class.getResourceAsStream("/fonts/Roboto-Regular.ttf")) {
            if (resourceAsStream == null) {
                System.err.println("Failed to load font!");
                System.exit(-1);
                return;
            }
            ImFont imFont = io.getFonts().addFontFromMemoryTTF(resourceAsStream.readAllBytes(), 15, 16);

            io.setFontDefault(imFont);
        } catch (IOException e) {
            System.err.println("Failed to load font!");
            e.printStackTrace();
            System.exit(-1);
            return;
        }

        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);  // Enable Keyboard Controls

        glfw.init(window, true);
        gl3.init("#version 330 core");

        ImGui.styleColorsDark();
        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(0);
        style.setFrameRounding(0);
        style.setChildRounding(0);
        style.setPopupRounding(0);
        style.setWindowBorderSize(0);
        style.setFramePadding(8, 8);
        style.setFrameBorderSize(1);
        style.setAntiAliasedFill(false);
        style.setAntiAliasedLines(false);
        style.setAntiAliasedLinesUseTex(false);
        style.setItemSpacing(8, 4);

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            style.setColor(ImGuiCol.Border, 1f, 1f, 1f, 1f);
            style.setColor(ImGuiCol.Button, 0f, 0f, 0f, 0f);
            style.setColor(ImGuiCol.ButtonHovered, 0f, 0f, 0f, .2f);
            style.setColor(ImGuiCol.ButtonActive, 1f, 1f, 1f, .2f);
            style.setColor(ImGuiCol.WindowBg, 1f, 1f, 1f, .2f);
        } else {
            style.setColor(ImGuiCol.Border, 0.2f, 0.2f, 0.2f, 1f);
            style.setColor(ImGuiCol.BorderShadow, 0f, 0f, 0f, 1f);
            style.setColor(ImGuiCol.Button, 1f, 0.5f, 0f, 1f);
            style.setColor(ImGuiCol.ButtonHovered, 1f, 0.75f, 0.5f, 1f);
            style.setColor(ImGuiCol.ButtonActive, .5f, 0.25f, 0f, 1f);
            style.setColor(ImGuiCol.WindowBg, 0.1f, 0.1f, 0.1f, 1f);
        }

        if (render(gl3, glfw, message, window)) return;
        if (render(gl3, glfw, message, window)) return;

        GLFW.glfwShowWindow(window);

        while (!GLFW.glfwWindowShouldClose(window)) {
            if (render(gl3, glfw, message, window)) return;
        }

        gl3.shutdown();
        glfw.shutdown();
        ImGui.destroyContext();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();

        System.exit(1);
    }

    private static boolean render(ImGuiImplGl3 gl3, ImGuiImplGlfw glfw, String message, long window) {
        GL11.glClearColor(0f, 0f, 0f, 0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        gl3.newFrame();
        glfw.newFrame();
        ImGui.newFrame();

        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX(), ImGui.getMainViewport().getPosY());
        ImGui.setNextWindowSize(ImGui.getMainViewport().getSizeX(), ImGui.getMainViewport().getSizeY());
        ImGui.begin("Permission Helper", ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoScrollbar);
        ImGui.textWrapped(message);

        String[] split = type.split(",");
        float sizeX = ImGui.getContentRegionAvailX() / split.length - ((split.length - 1) * 4);
        for (int i = 0; i < split.length; i++) {
            String type = split[i];
            if (ImGui.button(type, sizeX, 30)) {
                System.exit(i);
                return true;
            }
            if (i < split.length - 1) ImGui.sameLine();
        }

        GLFW.glfwSetWindowSize(window, 480, (int) ImGui.getCursorPosY() + 10);

        ImGui.end();
        ImGui.render();
        gl3.renderDrawData(ImGui.getDrawData());

        GLFW.glfwPollEvents();
        GLFW.glfwSwapBuffers(window);
        return false;
    }
}