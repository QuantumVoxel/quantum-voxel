package dev.ultreon.quantum.dedicated;

import com.badlogic.gdx.Gdx;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.debug.profiler.Profiler;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.text.ServerLanguage;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.DimensionInfo;
import dev.ultreon.quantum.world.WorldStorage;
import org.glassfish.tyrus.server.Server;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * DedicatedServer is a specific type of QuantumServer designed to run as a dedicated server
 * for the Quantum game platform.
 * <p>
 * It manages server-specific functionalities such as player connections, world storage, and
 * server language. It provides methods for starting, shutting down, and handling server crashes.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
@SuppressWarnings("GDXJavaStaticResource")
public class DedicatedServer extends QuantumServer {
    public static final ServerPlatform PLATFORM = Main.SERVER_PLATFORM;
    
    private static final WorldStorage STORAGE = new WorldStorage(Gdx.files.local("world"));
    private static final Profiler PROFILER = new Profiler();
    private static DedicatedServer instance;
    private final ServerLanguage language = createServerLanguage();
    private final Server server;

    /**
     * Creates a new dedicated server instance.
     *
     * @param host       the hostname for the server.
     * @param port       the port for the server.
     */
    public DedicatedServer(String host, int port, String path) {
        super(DedicatedServer.STORAGE, DedicatedServer.PROFILER);

        DedicatedServer.instance = this;

        server = new Server(host, port, null, null, JavaWebSocketServer.class);

        try {
            server.start();
            CommonConstants.LOGGER.info("WebSocket server running at ws://" + host + ":" + port + "/" + path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        GamePlatform.get().locateResources();
        GamePlatform.get().locateModResources();
    }

    public static DedicatedServer get() {
        return instance;
    }

    /**
     * Creates a new server language using the specified locale and language map.
     *
     * @return the newly created {@link ServerLanguage} object
     */
    @NotNull
    private ServerLanguage createServerLanguage() {
        // Specify the locale
        Locale locale = new Locale("en", "us");

        // Load the language resource from the file system
        InputStream resourceAsStream = getClass().getResourceAsStream("/data/quantum/lang/main.json");
        if (resourceAsStream == null) {
            throw new RuntimeException("Could not load language file!");

        }

        // Parse the language resource into a map
        Map<String, String> languageMap;
        try (InputStream resource = resourceAsStream) {
            InputStreamReader json = new InputStreamReader(
                    resource
            );

            //noinspection unchecked
//            languageMap = (Map<String, String>) CommonConstants.GSON.fromJson(json, Map.class);
            languageMap = Collections.emptyMap();
        } catch (IOException e) {
            throw new RuntimeException("Could not load language file!", e);
        }

        // Create and return a new ServerLanguage object
        return new ServerLanguage(locale, languageMap, new NamespaceID("quantum"));
    }

    /**
     * Constructor for the DedicatedServer class.
     *
     */
    DedicatedServer() {
        // Call the other constructor with hostname, port, and inspection
        this(ServerConfig.hostname, ServerConfig.port, ServerConfig.path);

        QuantumServer.LOGGER.info("Server started on {}:{}/{}", ServerConfig.hostname, ServerConfig.port, ServerConfig.path);

        try {
            // Create the world storage
            DedicatedServer.STORAGE.createWorld();
        } catch (IOException e) {
            // Throw a RuntimeException if an IOException occurs
            throw new RuntimeException(e);
        }

        // Set the maxPlayers from the config
        this.maxPlayers = ServerConfig.maxPlayers;
    }

    /**
     * Dedicated server crash handler.
     *
     * @param crashLog the throwable that caused the crash.
     */
    @Override
    public void crash(CrashLog crashLog) {
        ApplicationCrash crash = crashLog.createCrash();

        // Print and save the crash log.
        crash.printCrash();
        if (crash.getCrashLog().defaultSave().isFailure()) {
            CommonConstants.LOGGER.error("Failed to save crash log!", crash.getCrashLog().defaultSave().getFailure());
        }
    }

    /**
     * <p>
     * This will shutdown the server for the dedicated server.
     */
    @Override
    @Blocking
    public void shutdown() {
        super.shutdown();

        this.profiler.dispose();
        this.server.stop();
    }

    @Override
    protected void kickAllPlayers() {
        // Kick all the players and stop the connections.
        for (ServerPlayer player : this.getPlayers()) {
            player.kick("Server stopped");
        }
    }

    /**
     * This will crash and halt the server for the dedicated server.
     */
    @Override
    protected void onTerminationFailed() {
        this.crash(new Error("Termination failed!"));
        System.exit(1);
    }

    @Override
    public int getRenderDistance() {
        return ServerConfig.renderDistance;
    }

    @Override
    public void fatalCrash(Throwable throwable) {
        this.crash(throwable);
    }

    @Override
    public void run() {
        // Set up the spawn for the world
        this.dimManager.getWorld(DimensionInfo.OVERWORLD).setupSpawn();

        super.run();
    }

    @Override
    public boolean isTerminated() {
        return super.isTerminated() && !this.running && QuantumServer.get() == null;
    }

    public String handleTranslation(String path, Object[] args1) {
        String translation = language.get(path, args1);
        return translation == null ? path : translation;
    }
}
