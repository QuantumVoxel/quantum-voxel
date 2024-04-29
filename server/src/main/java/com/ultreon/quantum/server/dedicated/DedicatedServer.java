package com.ultreon.quantum.server.dedicated;

import com.ultreon.quantum.CommonConstants;
import com.ultreon.quantum.crash.ApplicationCrash;
import com.ultreon.quantum.crash.CrashLog;
import com.ultreon.quantum.debug.inspect.InspectionRoot;
import com.ultreon.quantum.debug.profiler.Profiler;
import com.ultreon.quantum.network.system.TcpNetworker;
import com.ultreon.quantum.server.QuantumServer;
import com.ultreon.quantum.server.player.ServerPlayer;
import com.ultreon.quantum.text.ServerLanguage;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.WorldStorage;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;

/**
 * Dedicated server implementation.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
@SuppressWarnings("GDXJavaStaticResource")
public class DedicatedServer extends QuantumServer {
    private static final WorldStorage STORAGE = new WorldStorage(Paths.get("world"));
    private static final Profiler PROFILER = new Profiler();
    private final ServerLanguage language = createServerLanguage();

    /**
     * Creates a new dedicated server instance.
     *
     * @param host       the hostname for the server.
     * @param port       the port for the server.
     * @param inspection the inspection root.
     * @throws UnknownHostException if the hostname cannot be resolved.
     */
    public DedicatedServer(String host, int port, InspectionRoot<Main> inspection) throws IOException {
        super(DedicatedServer.STORAGE, DedicatedServer.PROFILER, inspection);

        this.networker = new TcpNetworker(this, InetAddress.getByName(host), port);
    }

    /**
     * Creates a new server language using the specified locale and language map.
     *
     * @return the newly created {@link ServerLanguage} object
     */
    @NotNull
    private ServerLanguage createServerLanguage() {
        // Specify the locale
        Locale locale = Locale.of("en", "us");

        // Load the language resource from the file system
        InputStream resourceAsStream = getClass().getResourceAsStream("/assets/quantum/languages/main.json");
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
            languageMap = (Map<String, String>) CommonConstants.GSON.fromJson(json, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not load language file!", e);
        }

        // Create and return a new ServerLanguage object
        return new ServerLanguage(locale, languageMap, new Identifier("quantum"));
    }

    /**
     * Constructor for the DedicatedServer class.
     *
     * @param inspection the InspectionRoot object for main inspection
     * @throws UnknownHostException if the hostname is unknown
     */
    DedicatedServer(InspectionRoot<Main> inspection) throws IOException {
        // Call the other constructor with hostname, port, and inspection
        this(ServerConfig.hostname, ServerConfig.port, inspection);

        LOGGER.info("Server started on {}:{}", ServerConfig.hostname, ServerConfig.port);

        try {
            // Create the world storage
            DedicatedServer.STORAGE.createWorld();
        } catch (IOException e) {
            // Throw a RuntimeException if an IOException occurs
            throw new RuntimeException(e);
        }

        // Set the maxPlayers from the config
        this.maxPlayers = ServerConfig.maxPlayers;

        // Set up the spawn for the world
        this.world.setupSpawn();
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
     * {@inheritDoc}
     * <p>
     * This will shutdown the server for the dedicated server.
     */
    @Override
    @Blocking
    public void shutdown() {
        super.shutdown();

        this.profiler.dispose();
    }

    @Override
    protected void kickAllPlayers() {
        // Kick all the players and stop the connections.
        for (ServerPlayer player : this.getPlayers()) {
            player.kick("Server stopped");
        }
    }

    /**
     * {@inheritDoc}
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
    public boolean isTerminated() {
        return super.isTerminated() && !this.running && QuantumServer.get() == null;
    }

    public String handleTranslation(String path, Object[] args1) {
        String translation = language.get(path, args1);
        return translation == null ? path : translation;
    }
}
