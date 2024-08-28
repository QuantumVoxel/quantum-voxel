package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.esotericsoftware.kryo.kryo5.minlog.Log;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.CrashHandler;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.Acrylic;
import dev.ultreon.quantum.client.Main;
import dev.ultreon.quantum.GameWindow;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.WindowEvents;
import dev.ultreon.quantum.client.input.KeyAndMouseInput;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.js.JsLang;
import dev.ultreon.quantum.network.system.KyroNetSlf4jLogger;
import dev.ultreon.quantum.network.system.KyroSlf4jLogger;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.python.PyLang;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeCocoa;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DesktopLauncher {
    private static DesktopPlatform platform;
    private static DesktopWindow gameWindow;
    private static final boolean windowVibrancyEnabled = false;

    /**
     * Launches the game.
     * <p style="color:red;"><b>Note: This method should not be called.</b></p>
     *
     * @param argv the arguments to pass to the game
     */
    @ApiStatus.Internal
    public static void main(String[] argv) {
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

        platform = new DesktopPlatform() {
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
        };

        Log.setLogger(KyroSlf4jLogger.INSTANCE);
        com.esotericsoftware.minlog.Log.setLogger(KyroNetSlf4jLogger.INSTANCE);

        CrashHandler.addHandler(crashLog -> {
            try {
                KeyAndMouseInput.setCursorCaught(false);
                Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
                graphics.getWindow().setVisible(false);
            } catch (Exception e) {
                QuantumClient.LOGGER.error("Failed to hide cursor", e);
            }
        });

        try {
            Files.createDirectories(Paths.get("logs"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        QuantumClient.logDebug();

        new PyLang().init();
        new JsLang().init();

        // Before initializing LibGDX or creating a window:
        try (var ignored = GLFW.glfwSetErrorCallback((error, description) -> QuantumClient.LOGGER.error("GLFW Error: %s", description))) {
            try {
                new Lwjgl3Application(Main.createInstance(argv), DesktopLauncher.createConfig());
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
        config.setBackBufferConfig(4, 4, 4, 4, 8, 4, 0);
        config.setHdpiMode(HdpiMode.Pixels);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL32, 4, 1);
        config.setInitialVisible(false);
//        config.setDecorated(false);
        config.setTitle("Quantum");
        config.setWindowIcon(QuantumClient.getIcons());
        config.setWindowedMode(1280, 720);
        config.setWindowListener(new WindowAdapter());
//        config.setTransparentFramebuffer(true);

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        return config;
    }

    public static DesktopPlatform getPlatform() {
        return platform;
    }

    public boolean isWindowVibrancyEnabled() {
        return windowVibrancyEnabled;
    }

    private static class WindowAdapter extends Lwjgl3WindowAdapter {
        public static MessageDigest SHA_256;

        static {
            try {
                SHA_256 = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void created(Lwjgl3Window window) {
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.NOTIFICATION, false);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.LOW, false);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.MEDIUM, true);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.HIGH, true);

            gameWindow = new DesktopWindow(window);

            if (SharedLibraryLoader.isMac) {
                // Setup icons the mac way, using JNA.
                // So this requires native interaction.
                long l = GLFWNativeCocoa.glfwGetCocoaWindow(gameWindow.getHandle());


                try {
//                    final Image image = QuantumClient.getIconImage();
//                    Taskbar taskbar = Taskbar.getTaskbar();
//                    taskbar.setIconImage(image);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }

            WindowEvents.WINDOW_CREATED.factory().onWindowCreated(gameWindow);

            extractAcrylicNative();

            // Check for OS and apply acrylic/mica/vibrancy
            if (GamePlatform.get().isWindows()) {
                if (System.getProperty("os.name").startsWith("Windows 11") && (System.getProperty("os.arch").equals("x86_64") || System.getProperty("os.arch").equals("amd64"))) {
                    if (!Acrylic.applyMica(gameWindow.getPeer())) {
                        CommonConstants.LOGGER.warn("Unsupported Windows 11 build for mica background: {}", System.getProperty("os.name"));
                    }
                } else if (System.getProperty("os.name").startsWith("Windows 10")) {
                    if (!Acrylic.applyAcrylic(gameWindow.getPeer())) {
                        CommonConstants.LOGGER.warn("Unsupported Windows version for acrylic background: {}", System.getProperty("os.name"));
                    }
                } else {
                    CommonConstants.LOGGER.warn("Unsupported Windows version for blur background: {}", System.getProperty("os.name"));
                }
            }
        }

        private void extractAcrylicNative() {
            if (!GamePlatform.get().isWindows() && !GamePlatform.get().isMacOSX()) {
                return;
            }

            String osName = System.getProperty("os.name");
            if (osName.startsWith("Windows 11") || osName.startsWith("Windows 10")) {
                extractWindows();
            } else if (GamePlatform.get().isMacOSX()) {
                extractDarwin();
            } else {
                CommonConstants.LOGGER.warn("Unsupported operating system for blur background: {}", osName);
            }
        }

        private static void extractDarwin() {
            if (!System.getProperty("os.arch").equals("aarch64")) {
                CommonConstants.LOGGER.warn("Unsupported macOS architecture for vibrancy: {}", System.getProperty("os.arch"));
                return;
            }
            try {
                extractFile("lib/arm64/libacrylic.dylib", "libacrylic.dylib");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static void extractWindows() {
            if (!System.getProperty("os.arch").equals("x86_64") && !System.getProperty("os.arch").equals("amd64")) {
                CommonConstants.LOGGER.warn("Unsupported Windows architecture for acrylic/mica: {}", System.getProperty("os.arch"));
                return;
            }
            try {
                extractFile("lib/x64/acrylic.dll", "acrylic.dll");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static void extractFile(String providedPath, String extractedPath) throws IOException {
            String selfHash = getHash(DesktopLauncher.class.getResourceAsStream("/" + providedPath));
            Path path = Paths.get(extractedPath);
            if (Files.exists(path)) {
                InputStream curHash = Files.newInputStream(path);
                if (Objects.equals(selfHash, getHash(curHash))) {
                    CommonConstants.LOGGER.info("File {} is up to date", extractedPath);
                    return;
                }
            }
            InputStream resourceAsStream = DesktopLauncher.class.getResourceAsStream("/" + providedPath);
            if (resourceAsStream == null) {
                throw new RuntimeException("Failed to extract " + providedPath + " to " + extractedPath + " because it does not exist.");
            }
            Files.copy(resourceAsStream, path, StandardCopyOption.REPLACE_EXISTING);
        }

        private static String getHash(InputStream path) throws IOException {
            MessageDigest digest = SHA_256;

            byte[] hash = digest.digest(path.readAllBytes());
            return Base64.getEncoder().encodeToString(hash);
        }

        @Override
        public void focusLost() {
            QuantumClient.get().pause();

            WindowEvents.WINDOW_FOCUS_CHANGED.factory().onWindowFocusChanged(QuantumClient.get().getWindow(), false);
        }

        @Override
        public void focusGained() {
            WindowEvents.WINDOW_FOCUS_CHANGED.factory().onWindowFocusChanged(QuantumClient.get().getWindow(), true);
        }

        @Override
        public boolean closeRequested() {
            return !QuantumClient.get().tryShutdown();
        }

        @Override
        public void filesDropped(String[] files) {
            QuantumClient.get().filesDropped(files);

            WindowEvents.WINDOW_FILES_DROPPED.factory().onWindowFilesDropped(QuantumClient.get().getWindow(), files);
        }

    }
}
