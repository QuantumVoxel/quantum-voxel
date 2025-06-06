package dev.ultreon.quantum.lwjgl2;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.Os;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.CrashHandler;
import dev.ultreon.quantum.GameWindow;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.input.KeyAndMouseInput;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class DesktopLauncher {
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
        // Just override the load logic before any LibGDX class is touched
        SharedLibraryLoader.setLoaded("gdx");
        SharedLibraryLoader.setLoaded("openal");

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
     * @param args the arguments to pass to the game
     */
    @SuppressWarnings("unused")
    private static void launch(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS

        LauncherConfig launcherConfig = LauncherConfig.get();
        boolean useAngleGraphics = launcherConfig.useAngleGraphics && SharedLibraryLoader.os == Os.Windows;
        windowVibrancyEnabled = launcherConfig.windowVibrancyEnabled;
        fullVibrancyEnabled = launcherConfig.enableFullVibrancy;

        LauncherConfig.save();

        SafeLoadWrapper safeWrapper = new SafeLoadWrapper(args);
        platform = new DesktopPlatform(useAngleGraphics, safeWrapper) {
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

        CrashHandler.addHandler(crashLog -> {
            try {
                KeyAndMouseInput.setCursorCaught(false);
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

        try {
            new LwjglApplication(safeWrapper, DesktopLauncher.createConfig());
        } catch (ApplicationCrash e) {
            CrashLog crashLog = e.getCrashLog();
            QuantumClient.crash(crashLog);
        } catch (Throwable e) {
            platform.getLogger("CrashHandler").error("Failed to launch game", e);
            QuantumClient.crash(e);
        }
    }

    @NotNull
    private static LwjglApplicationConfiguration createConfig() {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.vSyncEnabled = false;
        config.foregroundFPS = 0;
        config.r = 4;
        config.g = 4;
        config.b = 4;
        config.a = 4;
        config.depth = 8;
        config.stencil = 8;
        config.useHDPI = false;
        config.title = "Quantum Voxel";
        config.width = 1280;
        config.height = 720;

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

}
