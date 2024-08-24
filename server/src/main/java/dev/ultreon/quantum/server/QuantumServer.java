package dev.ultreon.quantum.server;

import com.badlogic.gdx.utils.Disposable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.sun.jdi.connect.spi.ClosedConnectionException;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.api.events.server.ServerStartedEvent;
import dev.ultreon.quantum.api.events.server.ServerStartingEvent;
import dev.ultreon.quantum.api.events.server.ServerStoppedEvent;
import dev.ultreon.quantum.api.events.server.ServerStoppingEvent;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.debug.Debugger;
import dev.ultreon.quantum.debug.inspect.InspectionNode;
import dev.ultreon.quantum.debug.profiler.Profiler;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.events.WorldEvents;
import dev.ultreon.quantum.gamerule.GameRules;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;
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
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.server.player.CacheablePlayer;
import dev.ultreon.quantum.server.player.CachedPlayer;
import dev.ultreon.quantum.server.player.PermissionMap;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.PollingExecutorService;
import dev.ultreon.quantum.util.Shutdownable;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.WorldStorage;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * The base class for the Quantum Voxel server.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@ApiStatus.NonExtendable
public abstract class QuantumServer extends PollingExecutorService implements Runnable, Shutdownable {
    public static final int TPS = 20;
    public static final long NANOSECONDS_PER_SECOND = 1_000_000_000L;
    public static final long NANOSECONDS_PER_TICK = QuantumServer.NANOSECONDS_PER_SECOND / QuantumServer.TPS;

    public static final Logger LOGGER = LoggerFactory.getLogger("QuantumServer");
    @Deprecated(since = "0.1.0", forRemoval = true)
    public static final String NAMESPACE = "quantum";
    private static final ThreadGroup GROUP = new ThreadGroup("QuantumServer");
    private static final ThreadGroup WORLD_GEN_GROUP = new ThreadGroup("WorldGen");
    public static final ThreadFactory WORLD_GEN_THREAD_FACTORY = r -> {
        Thread thread1 = new Thread(WORLD_GEN_GROUP, r);
        thread1.setDaemon(true);
        thread1.setPriority(3);
        return thread1;
    };
    //    private static final WatchManager WATCH_MANAGER = new WatchManager(new ConfigurationScheduler("QuantumVoxel"));
    private static QuantumServer instance;
    private final List<Disposable> disposables = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(GROUP, r);
        thread.setName("QuantumServer");
        return thread;
    });
    private final Queue<Pair<ServerPlayer, Supplier<Packet<? extends ClientPacketHandler>>>> chunkNetworkQueue = new ArrayDeque<>();
    private final Map<UUID, ServerPlayer> players = new ConcurrentHashMap<>();
    protected Networker networker;
    private final WorldStorage storage;
    private final ResourceManager resourceManager;
    protected InspectionNode<QuantumServer> node;
    private InspectionNode<Object> playersNode;
    protected ServerWorld world;
    protected int port;
    protected int entityRenderDistance = 6 * World.CHUNK_SIZE;
    private int chunkRefresh;
    private long onlineTicks;
    protected volatile boolean running = false;
    private int currentTps;
    private boolean sendingChunk;
    protected int maxPlayers = 10;
    private final Cache<String, CachedPlayer> cachedPlayers = CacheBuilder.newBuilder().expireAfterAccess(24, TimeUnit.HOURS).build();
    private final Map<NamespaceID, ? extends ServerWorld> worlds;
    private final GameRules gameRules = new GameRules();
    private final PermissionMap permissions = new PermissionMap();
    private final CommandSender consoleSender = new ConsoleCommandSender();
    private final RecipeManager recipeManager;
    private final PlayerManager playerManager = new PlayerManager(this);

    /**
     * Creates a new {@link QuantumServer} instance.
     *
     * @param storage    the world storage for the world data.
     * @param parentNode the parent inspection node. (E.g., the client inspection node)
     */
    protected QuantumServer(WorldStorage storage, Profiler profiler, InspectionNode<?> parentNode) {
        super(profiler);

        ModApi.getGlobalEventHandler().call(new ServerStartingEvent(this));

        this.storage = storage;

        QuantumServer.instance = this;
        this.thread = new Thread(this, "server");

        this.networker = null;

        MapType worldData = new MapType();
        if (this.storage.exists("world.ubo")) {
            try {
                worldData = this.storage.read("world.ubo");
            } catch (IOException e) {
                this.crash(e);
            }
        }

        this.world = new ServerWorld(this, this.storage, worldData);

        // TODO: Make dimension registry.
        this.worlds = Map.of(
                new NamespaceID("overworld"), this.world // Overworld dimension. TODO: Add more dimensions.
        );

        if (DebugFlags.INSPECTION_ENABLED.isEnabled()) {
            this.node = parentNode.createNode("server", () -> this);
            this.playersNode = this.node.createNode("players", this.players::values);
            this.node.createNode("world", () -> this.world);
            this.node.create("refreshChunks", () -> this.chunkRefresh);
            this.node.create("renderDistance", this::getRenderDistance);
            this.node.create("entityRenderDistance", () -> this.entityRenderDistance);
            this.node.create("maxPlayers", () -> this.maxPlayers);
            this.node.create("tps", () -> this.currentTps);
            this.node.create("onlineTicks", () -> this.onlineTicks);
        }

        this.resourceManager = new ResourceManager("data");

        this.recipeManager = new RecipeManager(this);
        this.recipeManager.load(this.resourceManager);

        Recipes.init();
    }

//    public static WatchManager getWatchManager() {
//        return QuantumServer.WATCH_MANAGER;
//    }

    public void load() throws IOException {
        this.world.load();
    }

    public void save(boolean silent) throws IOException {
        try {
            this.world.save(silent);
        } catch (IOException e) {
            QuantumServer.LOGGER.error("Failed to save world", e);
        }
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
    @CanIgnoreReturnValue
    public static <T> T invokeAndWait(@NotNull Callable<T> func) {
        return QuantumServer.instance.submit(func).join();
    }

    /**
     * Submits a function to the server thread, and waits for it to complete.
     *
     * @param func the runnable to be executed.
     */
    public static void invokeAndWait(Runnable func) {
        QuantumServer.instance.submit(func).join();
    }

    /**
     * Submits a function to the server thread, and returns a future.
     *
     * @param func the runnable to be executed.
     * @return the future.
     */
    @CanIgnoreReturnValue
    public static @NotNull CompletableFuture<Void> invoke(Runnable func) {
        QuantumServer server = QuantumServer.instance;
        if (server == null) return CompletableFuture.failedFuture(new ServerStatusException("Server is offline!"));
        return server.submit(func);
    }

    /**
     * Submits a function to the server thread, and returns a future.
     *
     * @param func the callable to be executed.
     * @param <T>  the return type of the callable.
     * @return the future.
     */
    @CanIgnoreReturnValue
    public static <T> @NotNull CompletableFuture<T> invoke(Callable<T> func) {
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
        this.thread.start();
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
                    try {
                        // Tick the server.
                        this.runTick();
                    } catch (Throwable t) {
                        // Handle server tick error.
                        this.crash(new Throwable("Game being ticked.", t));
                    }
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

        // Close all connections.
        try {
            this.networker.close();
        } catch (ClosedChannelException | ClosedConnectionException e) {
            // Ignored
        } catch (IOException e) {
            // Log error for closing connections failure.
            QuantumServer.LOGGER.error("Closing connections failed!", e);
        }

        // Save all the server data.
        try {
            this.save(false);
        } catch (IOException e) {
            // Log error for saving server data failure.
            QuantumServer.LOGGER.error("Saving server data failed!", e);
        }

        // Cleanup any resources allocated.
        this.players.clear();
        this.scheduler.shutdownNow();

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
        // Create crash log.
        CrashLog crashLog = new CrashLog("Server crashed! :(", t);
        this.world.fillCrashInfo(crashLog);

        this.crash(crashLog);
        return crashLog;
    }

    /**
     * Stops the server thread in a clean state.
     * Note: this method is blocking.
     */
    @Override
    @Blocking
    public void shutdown() {
        // Send event for server stopping.
        kickAllPlayers();

        // Set running flag to make server stop.
        this.running = false;

        try {
            this.scheduler.shutdown();
            if (!this.scheduler.awaitTermination(60, TimeUnit.SECONDS) && !this.scheduler.isTerminated())
                this.onTerminationFailed();
        } catch (ApplicationCrash crash) {
            this.crash(crash.getCrashLog());
        } catch (Exception exc) {
            this.crash(exc);
        }

        try {
            this.thread.join(60000);
        } catch (InterruptedException e) {
            this.fatalCrash(new RuntimeException("Safe shutdown got interrupted."));
        }

        // Shut down the parent executor service.
        super.shutdownNow();
    }

    protected void kickAllPlayers() {

    }

    public abstract void crash(CrashLog crashLog);

    @OverridingMethodsMustInvokeSuper
    protected void runTick() {
        this.profiler.update();

        this.onlineTicks++;

        // Poll all the tasks in the queue.
        this.profiler.section("taskPolling", this::pollAll);

        // Tick connections.
        this.profiler.section("connections", this.networker::tick);

        // Tick the world.
        var world = this.world;
        if (world != null) {
            this.profiler.section("world", () -> {
                WorldEvents.PRE_TICK.factory().onPreTick(world);
                world.tick();
                WorldEvents.POST_TICK.factory().onPostTick(world);
            });
        }

        // Poll the chunk network queue.
        this.profiler.section("chunkPackets", this::pollChunkPacket);
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
     * Schedules a runnable to be executed after the specified delay.
     *
     * @param runnable the runnable.
     * @param time     the delay.
     * @param unit     the time unit of the delay.
     * @return the scheduled future.
     */
    public ScheduledFuture<?> schedule(Runnable runnable, long time, TimeUnit unit) {
        return this.scheduler.schedule(runnable, time, unit);
    }

    public void close() {
        for (Disposable disposable : this.disposables) {
            disposable.dispose();
        }

        this.world.dispose();

        this.recipeManager.unload();
        this.cachedPlayers.invalidateAll();
        this.players.forEach((UUID uuid, ServerPlayer player) -> {
            player.connection.disconnect("Server already closed!");
            this.cachedPlayers.invalidate(player.getName());
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
     * @return the game's version.
     */
    public String getGameVersion() {
//        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE);
//        if (container.isEmpty()) throw new InternalError("Can't find mod container for the base game.");
//        return container.get().getMetadata().getVersion().getFriendlyString();
        return "0.1.0";
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
        try {
            return this.cachedPlayers.get(name, () -> new CachedPlayer(null, name));
        } catch (ExecutionException e) {
            return null;
        }
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

        if (DebugFlags.INSPECTION_ENABLED.isEnabled()) {
            this.playersNode.createNode(player.getName(), () -> player);
        }

        // Send player to all other players within the render distance.
        var players = this.getPlayers()
                .stream()
                .toList();

        for (ServerPlayer other : players) {
            if (other == player) continue;
            Debugger.log("Player " + player.getName() + " is within the render distance of " + this.getEntityRenderDistance() + "!");
            other.connection.send(new S2CAddPlayerPacket(player.getUuid(), player.getName(), player.getPosition()));
            player.connection.send(new S2CAddPlayerPacket(other.getUuid(), other.getName(), other.getPosition()));
        }
    }

    /**
     * @return the server world.
     */
    public ServerWorld getWorld() {
        return this.world;
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
        QuantumServer.LOGGER.info("Player '%s' disconnected with message: %s", player.getName(), message);
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

    public boolean isRunning() {
        return this.running;
    }

    public boolean isIntegrated() {
        return false;
    }

    public final boolean isDedicated() {
        return !this.isIntegrated();
    }

    @Nullable
    public UUID getHost() {
        return null;
    }

    public ServerPlayer loadPlayer(String name, UUID uuid, IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        ServerPlayer player = new ServerPlayer(EntityTypes.PLAYER, this.world, uuid, name, connection);
        try {
            if (this.storage.exists(String.format("players/%s.ubo", name))) {
                QuantumServer.LOGGER.info("Loading player '%s'...", name);
                MapType read = this.storage.read(String.format("players/%s.ubo", name));
                player.load(read);
                player.markSpawned();
                player.markPlayedBefore();
                return player;
            }
        } catch (IOException e) {
            QuantumServer.LOGGER.warn("Failed to load player '" + name + "'!", e);
        }

        return player;
    }

    public boolean hasPlayedBefore(CacheablePlayer player) {
        return this.storage.exists("players/" + player.getName() + ".ubo");
    }

    public void handleWorldSaveError(Exception e) {

    }

    public void handleChunkLoadFailure(ChunkVec globalVec, String reason) {

    }

    public List<CachedPlayer> getCachedPlayers() {
        return Lists.newArrayList();
    }

    public Collection<? extends World> getWorlds() {
        return Collections.unmodifiableCollection(this.worlds.values());
    }

    public GameRules getGameRules() {
        return this.gameRules;
    }

    public ServerWorld getWorld(NamespaceID name) {
        return this.worlds.get(name);
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
        return this.worlds.values().stream().map(World::getEntities).flatMap(v -> Arrays.stream(v.toArray(Entity.class))).filter(entity -> entity.getUuid().equals(uuid)).findAny().orElse(null);
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
        Entity entityById = this.worlds.values().stream().map(World::getEntities).flatMap(v -> Arrays.stream(v.toArray(Entity.class))).filter(entity -> entity.getId() == id).findAny().orElse(null);

        if (type.isInstance(entityById)) {
            return type.cast(entityById);
        }

        return null;
    }

    public Map<String, ServerPlayer> getPlayersByName() {
        return Collections.unmodifiableMap(this.players.values().stream().collect(Collectors.toMap(ServerPlayer::getName, Function.identity())));
    }
}
