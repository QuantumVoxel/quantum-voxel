package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.utils.Os;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.github.dgzt.gdx.lwjgl3.Lwjgl3VulkanApplication;
import com.github.dgzt.gdx.lwjgl3.Lwjgl3WindowListener;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.CrashHandler;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.GameWindow;
import dev.ultreon.quantum.client.Main;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.WindowEvents;
import dev.ultreon.quantum.client.input.KeyAndMouseInput;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;

public class DesktopLauncher {
    public static final Logger LOGGER = LoggerFactory.getLogger("Quantum:Launcher");
    private static DesktopPlatform platform;
    private static DesktopWindow gameWindow;
    private static boolean windowVibrancyEnabled = false;
    private static boolean fullVibrancyEnabled = false;

    /**
     * Launches the game.
     * <p style="color:red;"><b>Note: This method should not be called.</b></p>
     *
     * @param argv the arguments to pass to the game
     */
    @ApiStatus.Internal
    public static void main(String[] argv) {
        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            private final Logger logger = LoggerFactory.getLogger("Quantum:ExceptionHandler");

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                try {
                    if (e instanceof ApplicationCrash) {
                        ApplicationCrash crash = (ApplicationCrash) e;
                        QuantumClient.crash(crash.getCrashLog());
                    }

                    defaultUncaughtExceptionHandler.uncaughtException(t, e);
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
            CommonConstants.LOGGER.error("Failed to launch game", e);
            CrashHandler.handleCrash(new CrashLog("Launch failed", e).createCrash().getCrashLog());
        }
    }

    /**
     * <h2 style="color:red;"><b>Note: This method should not be called.</b></h2>
     * Launches the game.
     * This method gets invoked dynamically by the FabricMC game provider.
     *
     * @param argv the arguments to pass to the game
     */
    @SuppressWarnings("unused")
    private static void launch(String[] argv) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS

        LauncherConfig launcherConfig = LauncherConfig.get();
        boolean useAngleGraphics = launcherConfig.useAngleGraphics && SharedLibraryLoader.os == Os.Windows;
        windowVibrancyEnabled = launcherConfig.windowVibrancyEnabled;
        fullVibrancyEnabled = launcherConfig.enableFullVibrancy;

        LauncherConfig.save();

        platform = new DesktopPlatform(useAngleGraphics) {
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
                return List.of();
            }

            @Override
            public boolean hasBackPanelRemoved() {
                return fullVibrancyEnabled && windowVibrancyEnabled && !useAngleGraphics;
            }

            @Override
            public void setFullVibrancy(boolean value) {
                LauncherConfig.get().enableFullVibrancy = value;
                LauncherConfig.save();
            }

            @Override
            public boolean getFullVibrancy() {
                return fullVibrancyEnabled;
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
        };

//        Log.setLogger(KyroSlf4jLogger.INSTANCE);
//        com.esotericsoftware.minlog.Log.setLogger(KyroNetSlf4jLogger.INSTANCE);

        CrashHandler.addHandler(crashLog -> {
            try {
                KeyAndMouseInput.setCursorCaught(false);
                if (gameWindow != null) {
                    gameWindow.setVisible(false);
                }
            } catch (Exception e) {
                QuantumClient.LOGGER.error("Failed to hide cursor", e);
            }
        });

        try {
            Files.createDirectories(Path.of("logs"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        QuantumClient.logDebug();

        // Before initializing LibGDX or creating a window:
        try (var ignored = GLFW.glfwSetErrorCallback((error, description) -> QuantumClient.LOGGER.error("GLFW Error: {}", description))) {
            try {
                if (GamePlatform.get().isAngleGLES()) new Lwjgl3VulkanApplication(Main.createInstance(argv), DesktopLauncher.createVulkanConfig());
                else new Lwjgl3Application(Main.createInstance(argv), DesktopLauncher.createConfig());
            } catch (ApplicationCrash e) {
                CrashLog crashLog = e.getCrashLog();
                QuantumClient.crash(crashLog);
            } catch (Throwable e) {
                platform.getLogger("CrashHandler").error("Failed to launch game", e);
                QuantumClient.crash(e);
            }
        }
    }

    private static com.github.dgzt.gdx.lwjgl3.Lwjgl3ApplicationConfiguration createVulkanConfig() {
        var config = new com.github.dgzt.gdx.lwjgl3.Lwjgl3ApplicationConfiguration();
        config.useVsync(false);
        config.setForegroundFPS(0);
        config.setIdleFPS(10);
        config.setBackBufferConfig(4, 4, 4, 4, 8, 0, 0);
        config.setHdpiMode(HdpiMode.Pixels);
        config.setOpenGLEmulation(com.github.dgzt.gdx.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES32, 4, 1);
        config.setInitialVisible(false);
        config.setTitle("Quantum Voxel (Vulkan Backend)");
        config.setWindowIcon(QuantumClient.getIcons());
        config.setWindowedMode(1280, 720);
        config.setWindowListener(new WindowAdapter());
        config.setTransparentFramebuffer(GamePlatform.get().hasBackPanelRemoved());

        return config;
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
//            WindowEvents.WINDOW_CREATED.factory().onWindowCreated(gameWindow);
            setupVibrancy(window.getWindowHandle());
        }

        @Override
        public void created(Lwjgl3Window window) {
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.NOTIFICATION, false);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.LOW, false);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.MEDIUM, true);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.HIGH, true);

            gameWindow = new DesktopGLWindow(window);

            setupMacIcon();
            WindowEvents.WINDOW_CREATED.factory().onWindowCreated(gameWindow);
            setupVibrancy(window.getWindowHandle());
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
            if (!SharedLibraryLoader.isMac && Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();

                if (taskbar != null) {
                    try {
                        if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                            InputStream res = DesktopLauncher.class.getResourceAsStream("/icon.png");
                            if (res != null) {
                                taskbar.setIconImage(ImageIO.read(res));
                            } else {
                                LOGGER.warn("Failed to extract icon.png");
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    if (taskbar.isSupported(Taskbar.Feature.ICON_BADGE_TEXT)) {
                        taskbar.setIconBadge("?");
                    }
                }
            }
        }

        @Override
        public void focusLost() {
            QuantumClient quantumClient = QuantumClient.get();
            if (quantumClient == null) return;
            quantumClient.pause();

            WindowEvents.WINDOW_FOCUS_CHANGED.factory().onWindowFocusChanged(quantumClient.getWindow(), false);
        }

        @Override
        public void focusGained() {
            QuantumClient quantumClient = QuantumClient.get();
            if (quantumClient == null) return;
            WindowEvents.WINDOW_FOCUS_CHANGED.factory().onWindowFocusChanged(quantumClient.getWindow(), true);
        }

        @Override
        public boolean closeRequested() {
            return !QuantumClient.get().tryShutdown();
        }

        @Override
        public void filesDropped(String[] files) {
            QuantumClient quantumClient = QuantumClient.get();
            if (quantumClient == null) return;
            quantumClient.filesDropped(files);

            WindowEvents.WINDOW_FILES_DROPPED.factory().onWindowFilesDropped(quantumClient.getWindow(), files);
        }

    }
}
