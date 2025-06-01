package dev.ultreon.quantum.dedicated;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import dev.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.config.QuantumServerConfig;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.dedicated.gui.DedicatedServerGui;
//import dev.ultreon.quantum.server.dedicated.http.ServerHttpSite;
import dev.ultreon.quantum.text.LanguageBootstrap;
import dev.ultreon.quantum.util.ModLoadingContext;
import org.jetbrains.annotations.ApiStatus;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Dedicated server main class.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
@ApiStatus.Internal
public class Main {
    static final ServerPlatform SERVER_PLATFORM = new ServerPlatform();

    private static final Logger LOGGER = LoggerFactory.getLogger("ServerMain");
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH.mm.ss");
    @SuppressWarnings("GDXJavaStaticResource")
    private static DedicatedServer server;

    /**
     * Main entry point for the server.
     * WARNING: Do not invoke.
     * This will be called by the FabricMC game provider.
     *
     * @param args command line arguments
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the current thread was interrupted
     */
    @SuppressWarnings("NewApi")
    @ApiStatus.Internal
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            LOGGER.info("Booting started!");
            new HeadlessApplication(new ApplicationAdapter() {

            }, createConfig());

            ModLoadingContext.withinContext(GamePlatform.get().getMod(CommonConstants.NAMESPACE).orElseThrow(), Main::initConfig);

            setupMods();

            LanguageBootstrap.bootstrap.set((path, args1) -> server != null ? server.handleTranslation(path, args1) : path);

            // Initialize the server loader.
            LOGGER.info("Loading server resources");
            ServerLoader serverLoader = new ServerLoader();
            serverLoader.load();

            // Start the server.
            LOGGER.info("Starting server");
            server = new DedicatedServer();
            server.init();
            server.start();

            // Handle server console commands.
            Scanner scanner = new Scanner(System.in);

            startGui();

            LOGGER.info("Server started!");

            console(scanner);
        } catch (ApplicationCrash e) {
            e.printCrash();
            e.handleCrash();
            e.getCrashLog().writeToFile(new File("crash-reports/crash-" + FORMAT.format(LocalDateTime.now()) + ".txt"));
        } catch (Throwable e) {
            serverCrash(e);
        }
    }

    private static void serverCrash(Throwable e) {
        // Server crashed! Saving a crash log and write it to the server console.
        CrashLog crashLog = new CrashLog("Server crashed!", e);
        crashLog.defaultSave();

        String string = crashLog.toString();
        Main.LOGGER.error(string);
        System.exit(1);
    }

    private static void console(Scanner scanner) throws InterruptedException {
        while (!Main.server.isTerminated()) {
            // Read command from the server console.
            if (scanner.hasNextLine()) {
                String commandline = scanner.nextLine();

                if (commandline.equals("stop")) {
                    // Handle stop command.
                    Main.server.shutdown(() -> {
                    });
                    if (!Main.server.awaitTermination(60, TimeUnit.SECONDS)) {
                        Main.server.onTerminationFailed();
                    }
                }
            }
        }
    }

    private static void startGui() {
        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {
                LOGGER.info("Starting server GUI");
                DedicatedServerGui gui = new DedicatedServerGui();
                gui.setVisible(true);
            });
        }
    }

    private static void setupMods() {
        // Invoke FabricMC entrypoint for dedicated server.
        LOGGER.info("Invoking FabricMC entrypoints");
        GamePlatform.get().invokeEntrypoint("main", ModInitializer.class, ModInitializer::onInitialize);
        GamePlatform.get().invokeEntrypoint("server", DedicatedServerModInitializer.class, DedicatedServerModInitializer::onInitializeServer);

        ModApi.init();
    }

    private static HeadlessApplicationConfiguration createConfig() {
        var config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 20;
        config.preferencesDirectory = "config";
        config.maxNetThreads = 32;
        return config;
    }

    /**
     * Gets the server instance.
     * Not recommended to use this method, use {@link QuantumServer#get()} instead.
     *
     * @return the server instance
     */
    @ApiStatus.Internal
    public static DedicatedServer getServer() {
        return Main.server;
    }

    private static void waitForKey() {
        // Check for docker
        if (System.getenv("DOCKER") != null) {
            return;
        }

        while (true) {
            try {
                if (System.in.read() != -1) {
                    break;
                }

                Thread.yield();

                if (Thread.interrupted()) {
                    break;
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to read from stdin", e);
                break;
            }
        }
    }

    private static void run() throws InterruptedException {
        ServerConfig serverConfig = new ServerConfig();
        new QuantumServerConfig();
        if (!serverConfig.getConfigPath().exists()) {
            serverConfig.save();

            Main.LOGGER.info("First-initialization finished, set up your config in server_config.quant and restart the server.");
            Main.LOGGER.info("We will wait 10 seconds so you would be able to stop the server for configuration.");

            Thread thread = new Thread(Main::waitForKey);
            thread.setDaemon(true);
            thread.setName("WaitForKey");
            thread.start();

            Duration.ofSeconds(10).sleep();

            thread.interrupt();
            thread.join();
        } else {
            serverConfig.load();
        }
    }

    private static void initConfig() {
        try {
            run();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize config", e);
        }
    }
}
