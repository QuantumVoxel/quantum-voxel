package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.utils.Os;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.github.dgzt.gdx.lwjgl3.Lwjgl3WindowListener;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import dev.ultreon.blockstudio.BlockStudio;
import dev.ultreon.quantum.CrashHandler;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.GameWindow;
import dev.ultreon.quantum.api.event.EventSystem;
import dev.ultreon.quantum.Margins;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.WindowEvent;
import dev.ultreon.quantum.client.input.KeyAndMouseInput;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.desktop.platform.win32.Dwmapi;
import dev.ultreon.quantum.desktop.platform.win32.MARGINS;
import dev.ultreon.quantum.desktop.platform.win32.RECT;
import dev.ultreon.quantum.desktop.platform.win32.UxTheme;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DesktopLauncher {
    public static final Logger LOGGER = LoggerFactory.getLogger("Quantum:Launcher");
    private static DesktopPlatform platform;
    private static DesktopWindow gameWindow;
    private static boolean windowVibrancyEnabled = false;
    private static boolean fullVibrancyEnabled = false;
    private static boolean fullAeroEnabled = false;
    private static SafeLoadWrapper safeWrapper;
    private static MARGINS aeroBounds;
    private static WinDef.HWND hwnd;

    /**
     * Launches the game.
     * <p style="color:red;"><b>Note: This method should not be called.</b></p>
     *
     * @param argv the arguments to pass to the game
     */
    @ApiStatus.Internal
    public static void main(String[] argv) {
        // Check for RenderDoc
        if (System.getProperty("renderdoc.path") != null) {
            String property = System.getProperty("renderdoc.path");
            if (property.endsWith(".dll")) {
                System.load(property);
            } else if (property.endsWith(".so")) {
                System.loadLibrary(property.substring(0, property.length() - 3));
            } else if (property.endsWith(".dylib")) {
                System.loadLibrary(property.substring(0, property.length() - 6));
            } else {
                System.loadLibrary(property);
            }

            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        }

        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            private final Logger logger = LoggerFactory.getLogger("Quantum:ExceptionHandler");

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                try {
                    if (Arrays.asList(argv).contains("--studio")) {
                        LoggerFactory.getLogger("Quantum:Studio").error("Failed to launch game", e);
                        defaultUncaughtExceptionHandler.uncaughtException(t, e);
                        return;
                    }
                    if (e instanceof ApplicationCrash) {
                        ApplicationCrash crash = (ApplicationCrash) e;
                        QuantumClient.crash(crash.getCrashLog());
                    }

                    logger.error("Failed to launch game", e);
                    GamePlatform.get().halt(StatusCode.forException());
                } catch (Throwable t1) {
                    try {
                        logger.error("Failed to handle exception", t1);
                        GamePlatform.get().halt(StatusCode.forException());
                    } catch (Throwable t2) {
                        GamePlatform.get().halt(StatusCode.forAbort());
                    }
                }

            }
        });

        try {
            DesktopLauncher.launch(argv);
        } catch (Exception | OutOfMemoryError e) {
            LOGGER.error("Failed to launch game", e);
        }
    }

    /**
     * <h2 style="color:red;"><b>Note: This method should not be called.</b></h2>
     * Launches the game.
     * This method gets invoked dynamically by the FabricMC game provider.
     *
     * @param args the arguments to pass to the game
     */
    @SuppressWarnings("unused")
    private static void launch(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS

        if (Arrays.asList(args).contains("--studio")) {
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.useVsync(false);
            config.setForegroundFPS(0);
            config.setIdleFPS(10);
            config.setBackBufferConfig(4, 4, 4, 4, 8, 8, 8);
            config.setHdpiMode(HdpiMode.Pixels);
            config.setWindowedMode(1280, 720);
            config.setTitle("Quantum Voxel (Block Studio)");
            try {
                Lwjgl3Application lwjgl3Application = new Lwjgl3Application(new BlockStudio(), config);
            } catch (Throwable e) {
                LOGGER.error("Failed to launch game", e);
                GamePlatform.get().halt(StatusCode.forException());
                return;
            }
            System.exit(0);
            return;
        }

        LauncherConfig launcherConfig = LauncherConfig.get();
        windowVibrancyEnabled = launcherConfig.windowVibrancyEnabled;
        fullVibrancyEnabled = launcherConfig.enableFullVibrancy;
        fullAeroEnabled = launcherConfig.enableFullAero;
        aeroBounds = launcherConfig.aeroBounds;

        LauncherConfig.save();

        safeWrapper = new SafeLoadWrapper(args) {
            @Override
            public void render() {
                if (fullAeroEnabled) {
                    // Open "Rebar" theme (used for insets in Explorer)
                    WinNT.HANDLE theme = UxTheme.INSTANCE.OpenThemeData(hwnd, new WString("Rebar"));
                    if (theme != null) {
                        WinDef.HDC hdc = new WinDef.HDC(new Pointer(GLFWNativeWin32.glfwGetWin32Window(gameWindow.getHandle())));
                        RECT rect = new RECT();
                        rect.left = 50;
                        rect.top = 50;
                        rect.right = 250;
                        rect.bottom = 100;

                        // Part/State IDs vary — 0/0 gives base background
                        UxTheme.INSTANCE.DrawThemeBackground(theme, hdc, 0, 0, rect, null);
                        UxTheme.INSTANCE.CloseThemeData(theme);
                    }
                }

                super.render();
            }
        };
        platform = new DesktopPlatform(false, safeWrapper) {
            @Override
            public GameWindow createWindow() {
                return gameWindow;
            }

            @Override
            public @Nullable MouseDevice getMouseDevice() {
                return null;
            }

            @Override
            public Collection<Device> getGameDevices() {
                return Arrays.asList();
            }

            @Override
            public boolean hasBackPanelRemoved() {
                return fullVibrancyEnabled && windowVibrancyEnabled || fullAeroEnabled;
            }

            @Override
            public void setFullVibrancy(boolean value) {
                LauncherConfig.get().enableFullVibrancy = value;
                LauncherConfig.save();
            }

            @Override
            public void setFullAero(boolean value) {
                LauncherConfig.get().enableFullAero = value;
                LauncherConfig.save();
            }

            @Override
            public boolean getFullVibrancy() {
                return fullVibrancyEnabled;
            }

            @Override
            public boolean getFullAero() {
                return fullAeroEnabled;
            }

            @Override
            public @Nullable Margins getAeroBounds() {
                return LauncherConfig.get().aeroBounds;
            }

            @Override
            public boolean isVibrancySupported() {
                return isWindows() && System.getProperty("os.version") != null && Integer.parseInt(System.getProperty("os.version").split("\\.")[0]) >= 10;
            }

            @Override
            public void setWindowVibrancy(boolean value) {
                LauncherConfig.get().windowVibrancyEnabled = value;
                LauncherConfig.save();
            }

            @Override
            public boolean getWindowVibrancy() {
                return windowVibrancyEnabled;
            }

            @Override
            public boolean isWeb() {
                return false;
            }
        };

        try {
            Files.createDirectories(Path.of("logs"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        QuantumClient.logDebug();

        // Before initializing LibGDX or creating a window:
        try (GLFWErrorCallback ignored = GLFW.glfwSetErrorCallback((error, description) -> QuantumClient.LOGGER.error("GLFW Error: {}", description))) {
            try {
                new Lwjgl3Application(safeWrapper, DesktopLauncher.createConfig());
            } catch (ApplicationCrash e) {
                CrashLog crashLog = e.getCrashLog();
                QuantumClient.crash(crashLog);
            } catch (Throwable e) {
                platform.getLogger("CrashHandler").error("Failed to launch game", e);
                QuantumClient.crash(e);
            }
        }
    }

    @NotNull
    private static Lwjgl3ApplicationConfiguration createConfig() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.useVsync(false);
        config.setForegroundFPS(0);
        config.setBackBufferConfig(4, 4, 4, 4, 8, 8, 0);
        config.setHdpiMode(HdpiMode.Pixels);
        config.setInitialVisible(false);
        config.setTitle("Quantum Voxel");
        config.setWindowIcon(QuantumClient.getIcons());
        config.setWindowedMode(1280, 720);
        config.setWindowListener(new WindowAdapter());
        if (platform.isWindows() || platform.isLinux()) {
            config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL32, 4, 1);
        }
        config.setTransparentFramebuffer(GamePlatform.get().hasBackPanelRemoved());

        return config;
    }

    public static DesktopPlatform getPlatform() {
        return platform;
    }

    public static GameWindow getGameWindow() {
        return gameWindow;
    }

    public boolean isWindowVibrancyEnabled() {
        return windowVibrancyEnabled;
    }

    private static class WindowAdapter extends Lwjgl3WindowAdapter implements Lwjgl3WindowListener {
        public static MessageDigest SHA_256;

        static {
            try {
                SHA_256 = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void created(com.github.dgzt.gdx.lwjgl3.Lwjgl3Window window) {
            gameWindow = new DesktopVulkanWindow(window);

            setupMacIcon();
            setupVibrancy(window.getWindowHandle());

            EventSystem.postDefault(new WindowEvent.Created(gameWindow));
        }

        @Override
        public void created(Lwjgl3Window window) {
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.NOTIFICATION, false);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.LOW, false);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.MEDIUM, true);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.HIGH, true);

            gameWindow = new DesktopGLWindow(window);

            setupMacIcon();

            if (fullAeroEnabled) {
                setupAero(window);
            } else {
                setupVibrancy(window.getWindowHandle());
            }

            EventSystem.postDefault(new WindowEvent.Created(gameWindow));
        }

        private void setupAero(Lwjgl3Window window) {
            if (SharedLibraryLoader.os != Os.Windows) {
                return;
            }

            // Extend glass into client area
            hwnd = new WinDef.HWND(new Pointer(GLFWNativeWin32.glfwGetWin32Window(window.getWindowHandle())));
            MARGINS margins = new MARGINS();
            margins.cxLeftWidth = aeroBounds.cxLeftWidth; // full window glass
            margins.cxRightWidth = aeroBounds.cxRightWidth; // full window glass
            margins.cyTopHeight = aeroBounds.cyTopHeight; // full window glass
            margins.cyBottomHeight = aeroBounds.cyBottomHeight; // full window glass
            Dwmapi.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, margins);
        }

        private static void setupVibrancy(long handle) {
            // Check for OS and apply acrylic/mica/vibrancy
            if (GamePlatform.get().isWindows()) {
                if (LauncherConfig.get().frameless) {
                    WindowUtils.makeWindowFrameless(handle);
                }

                WinDef.HWND hwnd = new WinDef.HWND(new Pointer(GLFWNativeWin32.glfwGetWin32Window(handle)));
                if (LauncherConfig.get().windowVibrancyEnabled) {
                    Dwmapi.setAcrylicBackground(hwnd);
                    Dwmapi.setUseImmersiveDarkMode(hwnd, true);
                }

                if (LauncherConfig.get().removeBorder) {
                    Dwmapi.removeBorder(hwnd);
                }
            } else if (GamePlatform.get().isMacOSX()) {
                // TODO: Implement vibrancy
            } else if (GamePlatform.get().isLinux()) {
                // TODO: Implement vibrancy
            }
        }

        private void setupMacIcon() {
            // No
        }

        @Override
        public void focusLost() {
            EventSystem.postDefault(new WindowEvent.FocusChanged(gameWindow, false));

            QuantumClient quantumClient = QuantumClient.get();
            if (quantumClient == null) return;
            quantumClient.pause();
        }

        @Override
        public void focusGained() {
            EventSystem.postDefault(new WindowEvent.FocusChanged(gameWindow, true));

            QuantumClient quantumClient = QuantumClient.get();
            if (quantumClient == null) return;
        }

        @Override
        public boolean closeRequested() {
            if (EventSystem.postCancelable(new WindowEvent.CloseRequested(gameWindow))) {
                return false;
            }

            if (safeWrapper.isCrashed()) {
                Runtime.getRuntime().halt(StatusCode.forAbort());
                return true;
            }

            return QuantumClient.get().tryShutdown();
        }

        @Override
        public void filesDropped(String[] files) {
            if (EventSystem.postCancelable(new WindowEvent.FilesDropped(gameWindow, files))) {
                return;
            }

            QuantumClient quantumClient = QuantumClient.get();
            if (quantumClient == null) return;
            quantumClient.filesDropped(files);
        }

        @Override
        public void iconified(boolean isIconified) {
            if (isIconified) {
                EventSystem.postDefault(new WindowEvent.Minimized(gameWindow));
            } else {
                EventSystem.postDefault(new WindowEvent.Restored(gameWindow));
            }
        }

        @Override
        public void maximized(boolean isMaximized) {
            if (isMaximized) {
                EventSystem.postDefault(new WindowEvent.Maximized(gameWindow));
            } else {
                EventSystem.postDefault(new WindowEvent.Restored(gameWindow));
            }
        }

        @Override
        public void refreshRequested() {
            EventSystem.postDefault(new WindowEvent.RefreshRequested(gameWindow));
        }
    }
}
