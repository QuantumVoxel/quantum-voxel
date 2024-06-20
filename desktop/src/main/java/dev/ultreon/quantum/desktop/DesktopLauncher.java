package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import dev.ultreon.quantum.CrashHandler;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.Main;
import dev.ultreon.quantum.GameWindow;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.WindowEvents;
import dev.ultreon.quantum.client.input.DesktopInput;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.js.JsLang;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.python.PyLang;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

public class DesktopLauncher {
    private static DesktopPlatform platform;
    private static DesktopWindow gameWindow;

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
            e.printStackTrace();
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
        FlatMacLightLaf.setup();

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

        CrashHandler.addHandler(crashLog -> {
            try {
                DesktopInput.setCursorCaught(false);
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

        GamePlatform.get().setupMacOSX();

        if (GamePlatform.get().isMacOSX()) {
            GamePlatform.get().setupMacOSX();
        }

        new PyLang().init();
        new JsLang().init();

        // Before initializing LibGDX or creating a window:
        try (var ignored = GLFW.glfwSetErrorCallback((error, description) -> QuantumClient.LOGGER.error("GLFW Error: %s", description))) {
            try {
                new Lwjgl3Application(Main.createInstance(argv), DesktopLauncher.createConfig());
            } catch (ApplicationCrash e) {
                CrashLog crashLog = e.getCrashLog();
                QuantumClient.crash(crashLog);
            } catch (Exception e) {
                QuantumClient.crash(e);
            }
        }
    }

    @NotNull
    private static Lwjgl3ApplicationConfiguration createConfig() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.useVsync(false);
        config.setForegroundFPS(0);
        config.setIdleFPS(10);
        config.setBackBufferConfig(4, 4, 4, 4, 8, 4, 0);
        config.setHdpiMode(HdpiMode.Pixels);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 3, 2);
        config.setInitialVisible(false);
        config.setDecorated(false);
        config.setTitle("Quantum");
        config.setWindowIcon(QuantumClient.getIcons());
        config.setWindowedMode(1280, 720);
        config.setWindowListener(new WindowAdapter());
        config.setTransparentFramebuffer(true);
        return config;
    }

    public static DesktopPlatform getPlatform() {
        return platform;
    }

    private static class WindowAdapter extends Lwjgl3WindowAdapter {

        @Override
        public void created(Lwjgl3Window window) {
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.NOTIFICATION, false);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.LOW, false);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.MEDIUM, true);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.HIGH, true);

            gameWindow = new DesktopWindow(window);

            WindowEvents.WINDOW_CREATED.factory().onWindowCreated(gameWindow);
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
            return QuantumClient.get().tryShutdown();
        }

        @Override
        public void filesDropped(String[] files) {
            QuantumClient.get().filesDropped(files);

            WindowEvents.WINDOW_FILES_DROPPED.factory().onWindowFilesDropped(QuantumClient.get().getWindow(), files);
        }

    }
}
