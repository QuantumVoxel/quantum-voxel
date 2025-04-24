package dev.ultreon.quantum.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Disposable;
import com.sun.jdi.connect.spi.ClosedConnectionException;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.api.events.server.ServerStartedEvent;
import dev.ultreon.quantum.api.events.server.ServerStartingEvent;
import dev.ultreon.quantum.api.events.server.ServerStoppedEvent;
import dev.ultreon.quantum.api.events.server.ServerStoppingEvent;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.debug.Debugger;
import dev.ultreon.quantum.debug.profiler.Profiler;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.events.WorldEvents;
import dev.ultreon.quantum.gamerule.GameRules;
import dev.ultreon.quantum.network.Networker;
import dev.ultreon.quantum.network.ServerStatusException;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.s2c.S2CAddPlayerPacket;
import dev.ultreon.quantum.network.packets.s2c.S2CRemovePlayerPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.recipe.RecipeManager;
import dev.ultreon.quantum.recipe.Recipes;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.registry.ServerRegistry;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.server.player.CacheablePlayer;
import dev.ultreon.quantum.server.player.CachedPlayer;
import dev.ultreon.quantum.server.player.PermissionMap;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.gen.biome.Biomes;
import dev.ultreon.quantum.world.gen.chunk.*;
import dev.ultreon.quantum.world.gen.noise.NoiseConfigs;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * The {@link QuantumServer} class represents the core server component of the QuantumGaming platform.
 * It manages all server operations including world data, player management, network communication, and more.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@ApiStatus.NonExtendable
public abstract class QuantumServer extends PollingExecutorService implements Runnable, Shutdownable {
    /**
     * TPS (Ticks Per Second) represents the number of ticks that occur
     * in one second. It is a crucial metric for timekeeping in game loops
     * and simulation software to ensure smooth and consistent updates.
     */
    public static final int TPS = 20;
    public static final int MSPT = 1000 / TPS;

    public static final long NANOSECONDS_PER_SECOND = 1_000_000_000L;
    public static final long NANOSECONDS_PER_TICK = QuantumServer.NANOSECONDS_PER_SECOND / QuantumServer.TPS;

    public static final Logger LOGGER = LoggerFactory.getLogger("QuantumServer");
    @Deprecated(since = "0.1.0", forRemoval = true)
    public static final String NAMESPACE = "quantum";
    private static final boolean CHUNK_DEBUG = System.getProperty("quantum.chunk.debug", "false").equals("true");

    @SuppressWarnings("GDXJavaStaticResource")
    private static QuantumServer instance;
    private final List<Disposable> disposables = new CopyOnWriteArrayList<>();
    private final Queue<Pair<ServerPlayer, Supplier<Packet<? extends ClientPacketHandler>>>> chunkNetworkQueue = new ArrayDeque<>();
    private final Map<UUID, ServerPlayer> players = new ConcurrentHashMap<>();
    private final MapType worldData;
    protected Networker networker;
    private final WorldStorage storage;
    private final ResourceManager resourceManager;
    //    protected InspectionNode<QuantumServer> node;
//    private InspectionNode<Object> playersNode;
    @ShowInNodeView
    protected int port;
    @ShowInNodeView
    protected int entityRenderDistance = 6 * World.CS;
    private long onlineTicks;
    protected volatile boolean running = false;
    private int currentTps;
    private boolean sendingChunk;
    protected int maxPlayers = 10;
    private final Map<String, CachedPlayer> cachedPlayers = new HashMap<>();
    private final GameRules gameRules = new GameRules();
    private final PermissionMap permissions = new PermissionMap();
    private final CommandSender consoleSender = new ConsoleCommandSender(this);
    private final RecipeManager recipeManager;
    private final PlayerManager playerManager = new PlayerManager(this);
    private final ServerRegistries registries = new ServerRegistries(this);
    protected final DimensionManager dimManager = new DimensionManager(this);
    private final Biomes biomes;
    private final NoiseConfigs noiseConfigs;

    @ShowInNodeView
    private long seed;

    /**
     * Creates a new {@link QuantumServer} instance.
     *
     * @param storage    the world storage for the world data.
     */
    protected QuantumServer(WorldStorage storage, Profiler profiler) {
        super(profiler);

        WorldSaveInfo worldSaveInfo = storage.loadInfo();
        seed = worldSaveInfo.seed();

        ModApi.getGlobalEventHandler().call(new ServerStartingEvent(this));

        this.storage = storage;

        QuantumServer.instance = this;
        if (!GamePlatform.get().isWeb()) this.thread = new Thread(this, "server");
        else this.thread = null;

        this.networker = null;

        MapType worldData = new MapType();
        if (this.storage.exists("world.ubo")) {
            try {
                worldData = this.storage.read("world.ubo");
            } catch (IOException e) {
                this.crash(e);
            }
        }

        this.worldData = worldData;

        this.noiseConfigs = new NoiseConfigs(this);
        this.biomes = new Biomes(this);
        this.add("Noise Configs", noiseConfigs);
        this.add("Biomes", biomes);
        this.add("Dimension Manager", this.dimManager);

        this.resourceManager = new ResourceManager("data");
        try {
            resourceManager.importPackage(Gdx.files.internal("."));
        } catch (IOException e) {
            LOGGER.warn("Server resources location is unknown!");
        }
        this.recipeManager = new RecipeManager(this);
        this.recipeManager.load(this.resourceManager);

        this.loadRegistries();

        for (Biome value : this.registries.get(RegistryKeys.BIOME).values()) {
            value.buildLayers();
        }

        reload(ReloadContext.create(this, this.resourceManager));

        Recipes.init();
    }

    private ServerWorld overworld() {
        return this.dimManager.getWorld(DimensionInfo.OVERWORLD);
    }

    private void loadRegistries() {
        var chunkGenRegistry = registries.get(RegistryKeys.CHUNK_GENERATOR);
        chunkGenRegistry.register(ChunkGenerator.OVERWORLD, CHUNK_DEBUG ? new DebugGenerator(this.registries.biomes()) : new OverworldGenerator(this.registries.biomes()));
        chunkGenRegistry.register(ChunkGenerator.TEST, new TestGenerator(this.registries.biomes()));
        chunkGenRegistry.register(ChunkGenerator.FLOATING_ISLANDS, new SpaceGenerator(this.registries.biomes()));

        var dimRegistry = registries.get(RegistryKeys.DIMENSION);
//        for (ResourceCategory dimensions : resourceManager.getResourceCategory("dimensions")) {
//            for (NamespaceID entry : dimensions.entries()) {
//                if (dimRegistry.contains(entry)) {
//                    LOGGER.warn("Dimension {} is already registered", entry);
//                    continue;
//                }
//
//                dimRegistry.register(
//                        entry.mapPath(path -> path.substring("dimensions/".length(), path.lastIndexOf('.'))),
//                        DimensionInfo.fromJson(dimensions.get(entry).readJson())
//                );
//            }
//        }

        dimRegistry.register(DimensionInfo.OVERWORLD, new DimensionInfo(
                NamespaceID.of("overworld"),
                Optional.empty(),
                ChunkGenerator.OVERWORLD
        ));

        dimRegistry.register(DimensionInfo.TEST, new DimensionInfo(
                NamespaceID.of("test"),
                Optional.empty(),
                ChunkGenerator.TEST
        ));

        dimRegistry.register(DimensionInfo.SPACE, new DimensionInfo(
                NamespaceID.of("space"),
                Optional.empty(),
                ChunkGenerator.FLOATING_ISLANDS
        ));

        this.dimManager.load(registries);
    }

    private void reload(ReloadContext context) {
        for (ServerRegistry<?> registry : registries.stream().collect(Collectors.toList()))
            registry.reload(context);

        this.recipeManager.reload(context);
    }

    public void load() throws IOException {

    }

    public void save(boolean silent) throws IOException {
        try {
            for (var worldEntry : dimManager.getWorlds().entrySet()) {
                if (!silent) LOGGER.info("Saving world {}", worldEntry.getKey());

                var world = worldEntry.getValue();
                if (world.isSaveable()) world.save(silent);
            }

            if (!silent) LOGGER.info("Saving world data");
            this.storage.write(this.worldData, "world.ubo");

            if (!silent) LOGGER.info("Saving player data");
            savePlayers(silent);
        } catch (IOException e) {
            QuantumServer.LOGGER.error("Failed to save world", e);
        }
    }

    private void savePlayers(boolean silent) throws IOException {
        for (var entry : this.players.values()) {
            UUID key = entry.getUuid();
            if (key.equals(getHost())) continue;
            savePlayer(silent, entry);
        }
    }

    protected void savePlayer(boolean silent, ServerPlayer entry) throws IOException {
        if (!silent) LOGGER.info("Saving player '{}'", entry.getName());
        MapType save = entry.save(new MapType());
        save.putString("dimension", entry.getWorld().getDimension().id().toString());
        this.storage.write(save, "players/" + entry.getUuid() + ".ubo");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    /**
     * Submits a function to the server thread, and waits for it to complete.
     *
     * @param func the callable to be executed.
     * @param <T>  the return type of the callable.
     * @return the result of the callable.
     */
    public static <T> T invokeAndWait(@NotNull Callable<T> func) {
        if (GamePlatform.get().isWeb() || Thread.currentThread() == QuantumServer.instance.thread()) {
            try {
                return func.call();
            } catch (Exception e) {
                throw new RejectedExecutionException("Failed to execute task", e);
            }
        }
        return QuantumServer.instance.submit(func).join();
    }

    /**
     * Submits a function to the server thread, and waits for it to complete.
     *
     * @param func the runnable to be executed.
     */
    public static void invokeAndWait(Runnable func) {
        if (GamePlatform.get().isWeb() || Thread.currentThread() == QuantumServer.instance.thread()) {
            func.run();
            return;
        }
        QuantumServer.instance.submit(func).join();
    }

    /**
     * Submits a function to the server thread, and returns a future.
     *
     * @param func the runnable to be executed.
     * @return the future.
     */
    public static @NotNull Promise<Void> invoke(Runnable func) {
        if (GamePlatform.get().isWeb() || Thread.currentThread() == QuantumServer.instance.thread()) {
            func.run();
            return CompletionPromise.completedFuture(null);
        }
        QuantumServer server = QuantumServer.instance;
        if (server == null) return Promise.failedFuture(new ServerStatusException("Server is offline!"));
        return server.submit(func);
    }

    /**
     * Submits a function to the server thread, and returns a future.
     *
     * @param func the callable to be executed.
     * @param <T>  the return type of the callable.
     * @return the future.
     */
    public static <T> @NotNull Promise<T> invoke(Callable<T> func) {
        return QuantumServer.instance.submit(func);
    }

    /**
     * Starts the server.
     * Note: Internal API.
     * This should only be invoked when you know what you are doing.
     * Improper usage may result in memory leaks, crashes or corruptions.
     *
     * @throws IllegalStateException if the server is already running.
     */
    @ApiStatus.Internal
    public void start() {
        if (this.running) throw new IllegalStateException("Server already running!");
        this.running = true;
        if (!GamePlatform.get().isWeb()) this.thread.start();
    }

    /**
     * @return the currently running server.
     */
    public static QuantumServer get() {
        return QuantumServer.instance;
    }

    /**
     * @return true if the current thread is the server thread.
     */
    public static boolean isOnServerThread() {
        if (GamePlatform.get().isWeb()) return true;
        QuantumServer instance = QuantumServer.instance;
        if (instance == null) throw new IllegalStateException("Server closed!");
        return instance.thread.getId() == Thread.currentThread().getId();
    }

    @ApiStatus.Internal
    public void addPlayer(ServerPlayer player) {
        this.players.put(player.getUuid(), player);
    }

    /**
     * The server main loop.
     * Note: Internal API.
     */
    @Override
    @ApiStatus.Internal
    public void run() {
        // Calculate tick duration based on TPS.
        var tickCap = 1000.0 / (double) QuantumServer.TPS;
        var tickTime = 0d;
        var gameFrameTime = 0d;
        var ticksPassed = 0;

        double time = System.currentTimeMillis();

        Duration sleepDuration = Duration.ofMilliseconds(5);
        try {
            // Send server started event to mods.
            ModApi.getGlobalEventHandler().call(new ServerStartedEvent(this));

            //* Main-loop.
            while (this.running) {
                var canTick = false;

                double time2 = System.currentTimeMillis();
                var passed = time2 - time;
                gameFrameTime += passed;
                tickTime += passed;

                time = time2;

                while (gameFrameTime >= tickCap) {
                    gameFrameTime -= tickCap;

                    canTick = true;
                }

                // Check if we can tick.
                if (canTick) {
                    ticksPassed++;
                    this.runTick();
                }

                // Calculate the current TPS every second.
                if (tickTime >= 1000.0d) {
                    this.currentTps = ticksPassed;
                    ticksPassed = 0;
                    tickTime = 0;
                }

                // Allow thread interrupting.
                sleepDuration.sleep();
            }

            // Server stopped.
            LOGGER.info("Stopping server...");
        } catch (InterruptedException ignored) {
            // Ignore interruption exception.
        } catch (Throwable t) {
            // Server crashed.
            this.crash(t);
            this.close();
            return;
        } finally {
            // Send server stopped event to mods.
            ModApi.getGlobalEventHandler().call(new ServerStoppingEvent(this));
        }

        // Save all the server data.
        try {
            this.save(false);
        } catch (IOException e) {
            // Log error for saving server data failure.
            QuantumServer.LOGGER.error("Saving server data failed!", e);
        }

        LOGGER.info("Server stopped.");

        // Close all connections.
        try {
            this.networker.close();
        } catch (ClosedChannelException | ClosedConnectionException e) {
            // Ignored
        } catch (IOException e) {
            // Log error for closing connections failure.
            QuantumServer.LOGGER.error("Closing connections failed!", e);
        }

        // Cleanup any resources allocated.
        this.players.clear();

        try {
            this.resourceManager.close();
            this.close();
        } catch (Exception e) {
            this.fatalCrash(e);
        }

        // Clear the instance.
        QuantumServer.instance = null;

        // Send event for server stopping to mods.
        ModApi.getGlobalEventHandler().call(new ServerStoppedEvent(this));

        // Log server stopped event.
        QuantumServer.LOGGER.info("Server stopped.");
    }

    /**
     * Crashes the server thread, or the client if we are the integrated server.
     *
     * @param t the throwable that caused the crash.
     * @return the crash log
     */
    public CrashLog crash(Throwable t) {
        LOGGER.error("Crash reported:", t);

        // Create crash log.
        CrashLog crashLog = new CrashLog("xServer crashed! :(", t);

        for (ServerWorld world : this.dimManager.getWorlds().values()) {
            world.fillCrashInfo(crashLog);
        }

        this.crash(crashLog);
        return crashLog;
    }

    /**
     * Initiates the shutdown process for the server. This method ensures that
     * all players are kicked, the scheduler is properly terminated, and the main
     * server thread is joined within a specified timeout. In case of failure during
     * termination or interruption, the appropriate crash handling is invoked.
     * <p>
     * This method blocks until the server shutdown process is complete or until the
     * specified timeout is reached.
     *
     * @throws RuntimeException if the safe shutdown process is interrupted.
     */
    @Override
    @Blocking
    public void shutdown() {
        // Send event for server stopping.
        kickAllPlayers();

        // Set running flag to make server stop.
        this.running = false;

        try {
            this.thread.join(60000);
        } catch (InterruptedException e) {
            this.fatalCrash(new RuntimeException("Safe shutdown got interrupted."));
        }

        // Shut down the parent executor service.
        super.shutdownNow();
    }

    /**
     * Removes all players from the game.
     * This method iterates through all connected players and removes them
     * from the game's player list. It may also perform additional cleanup
     * tasks necessary to reset the game state for all players being removed.
     * <p>
     * Note: Ensure that this method is called in a context where it is safe
     * to remove all players (e.g., game shutdown or reset scenario).
     */
    protected void kickAllPlayers() {

    }

    /**
     * Method to handle crash events by processing the provided crash log.
     *
     * @param crashLog the log information related to the crash event
     */
    public abstract void crash(CrashLog crashLog);

    /**
     * Executes a single server tick. This method performs various server-side operations
     * including profiler updates, task polling, connection handling, and world ticking.
     * <p>
     * Subclasses overriding this method must invoke this implementation to ensure basic server
     * operations are performed.
     */
    protected void runTick() {
        this.onlineTicks++;

        // Poll all the tasks in the queue.
        this.pollAll();

        // Tick connections.
        if (this.networker != null)
            this.networker.tick();

        // Tick the world.
        if (this.dimManager != null) {
            for (ServerWorld world : this.dimManager.getWorlds().values()) {
                if (world == null) continue;
                WorldEvents.PRE_TICK.factory().onPreTick(world);
                world.tick();
                WorldEvents.POST_TICK.factory().onPostTick(world);
            }
        }

        // Poll the chunk network queue.
        this.pollChunkPacket();
    }

    /**
     * Converts a time in seconds to ticks.
     *
     * @param seconds the time in seconds.
     * @return the number of ticks.
     */
    public static int seconds2ticks(float seconds) {
        return (int) (seconds * QuantumServer.TPS);
    }

    /**
     * Converts a time in minutes to ticks.
     *
     * @param minutes the time in minutes.
     * @return the number of ticks.
     */
    public static int minutes2ticks(float minutes) {
        return (int) (minutes * 60 * QuantumServer.TPS);
    }

    /**
     * Converts a time in hours to ticks.
     *
     * @param hours the time in hours.
     * @return the number of ticks.
     */
    public static int hours2ticks(float hours) {
        return (int) (hours * 3600 * QuantumServer.TPS);
    }

    private void pollChunkPacket() {
        if (this.sendingChunk) return;

        Pair<ServerPlayer, Supplier<Packet<? extends ClientPacketHandler>>> poll = this.chunkNetworkQueue.poll();
        if (poll != null) {
            this.sendingChunk = true;
            ServerPlayer first = poll.getFirst();
            Packet<? extends ClientPacketHandler> second = poll.getSecond().get();
            first.connection.send(second);
        }
    }

    /**
     * Gets the number of ticks since the server started.
     *
     * @return the number of ticks since the server started.
     */
    public long getOnlineTicks() {
        return this.onlineTicks;
    }

    /**
     * Defer a server disposable to be disposed when the server is closed.
     *
     * @param disposable the server disposable.
     * @param <T>        the type of the server disposable.
     * @return the same server disposable.
     */
    public <T extends Disposable> T disposeOnClose(T disposable) {
        this.disposables.add(disposable);
        return disposable;
    }

    /**
     * Closes the server instance and performs cleanup of resources.
     */
    public void close() {
        for (Disposable disposable : this.disposables) {
            disposable.dispose();
        }

        for (ServerWorld world : this.dimManager.getWorlds().values()) {
            world.dispose();
        }

        this.recipeManager.unload();
        this.cachedPlayers.clear();
        this.players.forEach((UUID uuid, ServerPlayer player) -> {
            player.connection.disconnect(CloseCodes.GOING_AWAY.getCode(), "Server closed!");
            this.cachedPlayers.remove(player.getName());
        });
        this.players.clear();
        this.resourceManager.close();
    }

    /**
     * Handles the failed termination of the server.
     */
    protected abstract void onTerminationFailed();

    /**
     * @return the server port.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * @return the render distance.
     */
    public abstract int getRenderDistance();

    /**
     * @return the entity render distance.
     */
    public int getEntityRenderDistance() {
        return this.entityRenderDistance;
    }

    /**
     * Retrieves the game version based on the mod container's metadata.
     *
     * @return Friendly string representation of the game's version.
     * @throws InternalError if the mod container for the base game cannot be found.
     */
    public String getGameVersion() {
//        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE);
//        if (container.isEmpty()) throw new InternalError("Can't find mod container for the base game.");
//        return container.get().getMetadata().getVersion().getFriendlyString();
        return "0.2.0";
    }

    /**
     * Get the player with the specified uuid.
     *
     * @param uuid the uuid of the player.
     * @return the player, or null if not found.
     */
    public @Nullable ServerPlayer getPlayer(UUID uuid) {
        return this.players.get(uuid);
    }

    /**
     * Get the player with the specified name.
     *
     * @param name the name of the player
     * @return the player, or null if not found.
     */
    public @Nullable ServerPlayer getPlayer(String name) {
        for (ServerPlayer player : this.players.values()) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null;
    }

    /**
     * Gets a player from the cache.
     *
     * @param name the name of the player
     * @return the player, or null if the player is not in the cache.
     */
    public @Nullable CachedPlayer getCachedPlayer(String name) {
        return this.cachedPlayers.putIfAbsent(name, new CachedPlayer(null, name));
    }

    /**
     * Gets a player from the cache.
     *
     * @param uuid the uuid of the player
     * @return the player, or null if the player is not in the cache.
     */
    public @Nullable CachedPlayer getCachedPlayer(UUID uuid) {
//        return this.cachedPlayers.get(name, () -> new CachedPlayer(uuid, null));
        return null;
    }

    /**
     * Places a player into the server.
     *
     * @param player the player to place.
     */
    @ApiStatus.Internal
    public void placePlayer(ServerPlayer player) {
        // Put the player into the player list.
        this.players.put(player.getUuid(), player);
        this.cachedPlayers.put(player.getName(), new CachedPlayer(player.getUuid(), player.getName()));

        // Send player to all other players within the render distance.
        var players = this.getPlayers()
                .stream()
                .collect(Collectors.toList());

        for (ServerPlayer other : players) {
            if (other == player) continue;
            Debugger.log("Player " + player.getName() + " is within the render distance of " + this.getEntityRenderDistance() + "!");
            other.connection.send(new S2CAddPlayerPacket(player.getId(), player.getUuid(), player.getName(), player.getPosition()));
            player.connection.send(new S2CAddPlayerPacket(other.getId(), other.getUuid(), other.getName(), other.getPosition()));
        }
    }

    /**
     * @return the server world.
     */
    @Deprecated
    public ServerWorld getWorld() {
        return this.dimManager.getWorld(DimensionInfo.OVERWORLD);
    }

    /**
     * @return the overworld.
     */
    public ServerWorld getOverworld() {
        return this.dimManager.getWorld(DimensionInfo.OVERWORLD);
    }

    /**
     * @return the server connections.
     */
    public Networker getNetworker() {
        return this.networker;
    }

    /**
     * @return the world storage.
     */
    public WorldStorage getStorage() {
        return this.storage;
    }

    /**
     * @return all players in the server.
     */
    public Collection<ServerPlayer> getPlayers() {
        return this.players.values();
    }

    /**
     * @return the current TPS.
     */
    public int getCurrentTps() {
        return this.currentTps;
    }

    /**
     * Called when the player has disconnected from the server.
     *
     * @param player  the player that disconnected.
     * @param message the disconnect message.
     */
    @ApiStatus.Internal
    public void onDisconnected(ServerPlayer player, String message) {
        QuantumServer.LOGGER.info("Player '{}' disconnected with message: {}", player.getName(), message);
        try {
            savePlayer(false, player);
        } catch (IOException e) {
            QuantumServer.LOGGER.warn("Failed to save player '{}'!", player.getName(), e);
        }
        this.players.remove(player.getUuid());
        for (ServerPlayer other : this.players.values()) {
            other.connection.send(new S2CRemovePlayerPacket(other.getUuid()));
        }
    }

    /**
     * Get the players in the specified chunk.
     *
     * @param pos the chunk to find players in.
     * @return the players in the specified chunk.
     */
    public Stream<ServerPlayer> getPlayersInChunk(ChunkVec pos) {
        return this.players.values().stream().filter(player -> player.getChunkVec().equals(pos));
    }

    /**
     * @return the maximum number of players configured in the server.
     */
    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    /**
     * @return the number of players currently in the server.
     */
    public int getPlayerCount() {
        return this.players.size();
    }

    /**
     * Checks if the server is currently running.
     *
     * @return true if the server is running, false otherwise.
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * Checks if the server is integrated with the client.
     *
     * @return true if this is the integrated server
     */
    public boolean isIntegrated() {
        return false;
    }

    /**
     * Checks if the server is dedicated.
     *
     * @return true if this is the dedicated server
     */
    public final boolean isDedicated() {
        return !this.isIntegrated();
    }

    /**
     * Retrieves the UUID of the host.
     *
     * @return the UUID of the host, or null if no host is available
     */
    @Nullable
    public UUID getHost() {
        return null;
    }

    public ServerPlayer loadPlayer(String name, UUID uuid, IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        try {
            String path = "players/" + uuid + ".ubo";
            if (this.storage.exists(path)) {
                QuantumServer.LOGGER.info("Loading player '{}'...", name);
                MapType read = this.storage.read(path);
                NamespaceID dimId = NamespaceID.tryParse(read.getString("dimension"));
                if (dimId == null) {
                    QuantumServer.LOGGER.warn("Failed to properly load player '{}', missing dimension!", name);
                    dimId = NamespaceID.of("overworld");
                }
                RegistryKey<DimensionInfo> key = RegistryKey.of(RegistryKeys.DIMENSION, dimId);
                ServerWorld world = this.dimManager.getWorld(key);
                if (world == null) {
                    QuantumServer.LOGGER.warn("Failed to properly load player '{}', unknown dimension '{}'!", name, dimId);
                    world = this.dimManager.getWorld(DimensionInfo.OVERWORLD);
                }
                ServerPlayer player = new ServerPlayer(EntityTypes.PLAYER, world, uuid, name, connection);
                player.load(read);
                player.markPlayedBefore();
                return player;
            }
        } catch (IOException e) {
            QuantumServer.LOGGER.warn("Failed to load player '{}'!", name, e);
        }

        return new ServerPlayer(EntityTypes.PLAYER, this.dimManager.getWorld(DimensionInfo.OVERWORLD), uuid, name, connection);
    }

    public boolean hasPlayedBefore(CacheablePlayer player) {
        return this.storage.exists("players/" + player.getUuid() + ".ubo");
    }

    public void handleWorldSaveError(Exception e) {

    }

    public void handleChunkLoadFailure(ChunkVec globalVec, String reason) {

    }

    public List<CachedPlayer> getCachedPlayers() {
        return new ArrayList<>();
    }

    public Collection<? extends World> getWorlds() {
        return Collections.unmodifiableCollection(this.dimManager.getWorlds().values());
    }

    public GameRules getGameRules() {
        return this.gameRules;
    }

    public ServerWorld getWorld(NamespaceID name) {
        return this.dimManager.getWorld(RegistryKey.of(RegistryKeys.DIMENSION, name));
    }

    public PermissionMap getDefaultPermissions() {
        return this.permissions;
    }

    public @Nullable CacheablePlayer getCacheablePlayer(String name) {
        ServerPlayer player = this.getPlayer(name);
        if (player != null) return player;

        return this.getCachedPlayer(name);
    }

    public CacheablePlayer getCacheablePlayer(UUID uuid) {
        return null; // TODO: Implement cacheable players by uuid.
    }

    public @Nullable Entity getEntity(@NotNull UUID uuid) {
        return this.dimManager.getWorlds().values().stream().map(World::getEntities).flatMap(v -> Arrays.stream(v.toArray(Entity.class))).filter(entity -> entity.getUuid().equals(uuid)).findAny().orElse(null);
    }

    public CommandSender getConsoleSender() {
        return this.consoleSender;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public RecipeManager getRecipeManager() {
        return this.recipeManager;
    }

    public abstract void fatalCrash(Throwable throwable);

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    @Nullable
    @ApiStatus.Experimental
    public final <T extends Entity> T getEntity(int id, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();
        Entity entityById = this.dimManager.getWorlds().values().stream().map(World::getEntities).flatMap(v -> Arrays.stream(v.toArray(Entity.class))).filter(entity -> entity.getId() == id).findAny().orElse(null);

        if (type.isInstance(entityById)) {
            return type.cast(entityById);
        }

        return null;
    }

    public Map<String, ServerPlayer> getPlayersByName() {
        return Collections.unmodifiableMap(this.players.values().stream().collect(Collectors.toMap(ServerPlayer::getName, Function.identity())));
    }

    public void onChunkBuilt(ServerChunk builtChunk) {

    }

    public void onChunkLoaded(ServerChunk loadedChunk) {

    }

    public void onChunkFailedToLoad(Vec3d d) {

    }

    public void onChunkUnloaded(ServerChunk unloadedChunk) {

    }

    public void onChunkLoadRequested(ChunkVec globalVec) {

    }

    public void onChunkSent(ServerChunk serverChunk) {

    }

    public ServerRegistries getRegistries() {
        return registries;
    }

    public Biomes getBiomes() {
        return this.biomes;
    }

    public NoiseConfigs getNoiseConfigs() {
        return noiseConfigs;
    }

    /**
     * Retrieves the DimensionManager instance associated with this object.
     *
     * @return the current instance of DimensionManager.
     */
    public DimensionManager getDimManager() {
        return dimManager;
    }

    public void init() {
        this.dimManager.setDefaults(registries);

        this.dimManager.loadWorlds(seed);
    }

    public void addGizmo(BoundingBox boundingBox, Color color) {

    }

    public Thread thread() {
        return thread;
    }

    public Stream<Entity> getEntities() {
        return this.dimManager.getWorlds().values().stream().map(World::getEntities).flatMap(v -> Arrays.stream(v.toArray(Entity.class)));
    }
}
