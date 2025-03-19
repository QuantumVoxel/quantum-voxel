package dev.ultreon.quantum.server.dedicated;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.esotericsoftware.kryo.kryo5.minlog.Log;
import dev.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.config.QuantumServerConfig;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.debug.inspect.InspectionRoot;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;
import dev.ultreon.quantum.network.system.KyroNetSlf4jLogger;
import dev.ultreon.quantum.network.system.KyroSlf4jLogger;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.dedicated.gui.DedicatedServerGui;
import dev.ultreon.quantum.server.dedicated.http.ServerHttpSite;
import dev.ultreon.quantum.text.LanguageBootstrap;
import dev.ultreon.quantum.util.ModLoadingContext;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Dedicated server main class.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
@ApiStatus.Internal
public class Main {
    static final ServerPlatform SERVER_PLATFORM = new ServerPlatform();

    private static final Logger LOGGER = LoggerFactory.getLogger("ServerMain");
    private static DedicatedServer server;
    private static ServerLoader serverLoader;
    static ServerHttpSite site;

    /**
     * Main entry point for the server.
     * WARNING: Do not invoke.
     * This will be called by the FabricMC game provider.
     *
     * @param args command line arguments
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the current thread was interrupted
     */
    @ApiStatus.Internal
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            Log.setLogger(KyroSlf4jLogger.INSTANCE);
            com.esotericsoftware.minlog.Log.setLogger(KyroNetSlf4jLogger.INSTANCE);

            LOGGER.info("Booting started!");
            HeadlessApplication app = new HeadlessApplication(new ApplicationAdapter() {

            }, createConfig());

//            AtomicBoolean waitingForLanguageBindings = new AtomicBoolean(true);
//            JDialog progressDialog = new JDialog();
//            progressDialog.setModal(true);
//
//            progressDialog.setLayout(new BoxLayout(progressDialog.getContentPane(), BoxLayout.Y_AXIS));
//
//            JLabel messageLabel = new JLabel();
//            progressDialog.add(messageLabel);
//
//            JProgressBar progressBar = new JProgressBar();
//            progressDialog.add(progressBar);
//
//            progressDialog.pack();
//
//            AtomicBoolean preprocessing = new AtomicBoolean(true);
//
//            LangGenConfig.progressListener = new LangGenListener() {
//                @Override
//                public void onProgress(int progress, int total) {
//                    preprocessing.set(false);
//                    messageLabel.setText("Generating language bindings: " + progress + "/" + total);
//                    progressBar.setValue(progress);
//                    progressBar.setMaximum(total);
//                }
//
//                @Override
//                public void onPreprocessProgress(int progress, int total) {
//                    if (!preprocessing.get()) return;
//                    messageLabel.setText("Preprocessing language bindings: " + progress + "/" + total);
//                    progressBar.setValue(progress);
//                    progressBar.setMaximum(total);
//                }
//
//                @Override
//                public void onDone() {
//                    waitingForLanguageBindings.set(false);
//                    progressDialog.dispose();
//                }
//            };
//            LangGenMain.genBindings();
//
//            while (waitingForLanguageBindings.get()) {
//                Thread.sleep(100);
//            }

            ModLoadingContext.withinContext(GamePlatform.get().getMod(CommonConstants.NAMESPACE).orElseThrow(), Main::initConfig);

            // Invoke FabricMC entrypoint for dedicated server.
            LOGGER.info("Invoking FabricMC entrypoints");
            FabricLoader.getInstance().invokeEntrypoints("main", ModInitializer.class, ModInitializer::onInitialize);
            FabricLoader.getInstance().invokeEntrypoints("server", DedicatedServerModInitializer.class, DedicatedServerModInitializer::onInitializeServer);

            Thread httpThread = new Thread(() -> {
                LOGGER.info("Starting HTTP server");
                try {
                    Main.site = new ServerHttpSite();
                } catch (IOException e) {
                    CommonConstants.LOGGER.error("Failed to start HTTP server", e);
                }
            });
            httpThread.setDaemon(true);
            httpThread.start();

            ModApi.init();

            LanguageBootstrap.bootstrap.set((path, args1) -> server != null ? server.handleTranslation(path, args1) : path);

            // Initialize the server loader.
            LOGGER.info("Loading server resources");
            serverLoader = new ServerLoader();
            serverLoader.load();

            // Start the server.
            LOGGER.info("Starting server");
            @SuppressWarnings("InstantiationOfUtilityClass") var inspection = new InspectionRoot<>(new Main());
            server = new DedicatedServer(inspection);
            server.init();
            server.start();

            // Handle server console commands.
            Scanner scanner = new Scanner(System.in);

            if (!GraphicsEnvironment.isHeadless()) {
                SwingUtilities.invokeLater(() -> {
                    LOGGER.info("Starting server GUI");
                    DedicatedServerGui gui = new DedicatedServerGui();
                    gui.setVisible(true);
                });
            }

            LOGGER.info("Server started!");

            while (!Main.server.isTerminated()) {
                // Read command from the server console.
                if (scanner.hasNextLine()) {
                    String commandline = scanner.nextLine();

                    if (commandline.equals("stop")) {
                        // Handle stop command.
                        Main.server.shutdown();
                        if (!Main.server.awaitTermination(60, TimeUnit.SECONDS)) {
                            Main.server.onTerminationFailed();
                        }
                    }
                }
            }
        } catch (ApplicationCrash e) {
            e.getCrashLog().createCrash().printCrash();
        } catch (Throwable e) {
            // Server crashed! Saving a crash log, and write it to the server console.
            CrashLog crashLog = new CrashLog("Server crashed!", e);
            crashLog.defaultSave();

            String string = crashLog.toString();
            Main.LOGGER.error(string);
            System.exit(1);
        }
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

                Thread.sleep(10);
            } catch (IOException e) {
                LOGGER.warn("Failed to read from stdin", e);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Failed to sleep", e);
                break;
            }
        }
    }

    private static void run() throws InterruptedException {
        ServerConfig serverConfig = new ServerConfig();
        new QuantumServerConfig();
        if (!Files.exists(serverConfig.getConfigPath(), LinkOption.NOFOLLOW_LINKS)) {
            serverConfig.save();

            Main.LOGGER.info("First-initialization finished, set up your config in server_config.json5 and restart the server.");
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
