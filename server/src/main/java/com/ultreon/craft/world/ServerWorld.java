package com.ultreon.craft.world;

import com.badlogic.gdx.math.MathUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Queues;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.block.entity.BlockEntity;
import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.config.UltracraftServerConfig;
import com.ultreon.craft.debug.ValueTracker;
import com.ultreon.craft.debug.WorldGenDebugContext;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.s2c.S2CAddEntityPacket;
import com.ultreon.craft.network.packets.s2c.S2CAddPlayerPacket;
import com.ultreon.craft.network.packets.s2c.S2CBlockEntitySetPacket;
import com.ultreon.craft.network.packets.s2c.S2CBlockSetPacket;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.util.*;
import com.ultreon.craft.world.gen.TerrainGenerator;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseConfigs;
import com.ultreon.data.DataIo;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.LongType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ServerWorld extends World {
    private final WorldStorage storage;
    private final UltracraftServer server;
    private final RegionStorage regionStorage = new RegionStorage();
    @Nullable
    private CompletableFuture<Boolean> saveFuture;
    @Nullable
    private ScheduledFuture<?> saveSchedule;
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "ServerWorld-Save");
        thread.setDaemon(true);
        return thread;
    });
    private static long chunkUnloads;

    private final TerrainGenerator terrainGen;

    private final Queue<ChunkPos> chunksToLoad = this.createSyncQueue();
    private final Queue<ChunkPos> chunksToUnload = this.createSyncQueue();
    private final Queue<Runnable> tasks = this.createSyncQueue();

    private final Lock chunkLock = new ReentrantLock();

    private int playTime;
    private final Set<RecordedChange> recordedChanges = new CopyOnWriteArraySet<>();
    private int chunksToLoadCount;
    private boolean saving;

    public ServerWorld(UltracraftServer server, WorldStorage storage, MapType worldData) {
        super((LongType) worldData.get("seed"));
        this.server = server;
        this.storage = storage;

        this.load(worldData);

        final var biomeDomain = new DomainWarping(NoiseConfigs.BIOME_X.create(this.seed), NoiseConfigs.BIOME_Y.create(this.seed));
        final var layerDomain = new DomainWarping(NoiseConfigs.LAYER_X.create(this.seed), NoiseConfigs.LAYER_Y.create(this.seed));

        this.terrainGen = new TerrainGenerator(biomeDomain, layerDomain, NoiseConfigs.BIOME_MAP);

        for (Biome value : Registries.BIOME.values()) {
            if (value.doesNotGenerate()) continue;
            this.terrainGen.registerBiome(this, this.getSeed(), value, value.getTemperatureStart(), value.getTemperatureEnd(), value.isOcean());
        }

        this.terrainGen.create(this, this.seed);
    }

    private void load(MapType worldData) {
        this.playTime = worldData.getInt("playTime", 0);
        this.uid = worldData.getUUID("uid", this.uid);
        this.spawnPoint.set(worldData.getInt("spawnX", 0), worldData.getInt("spawnY", 0), worldData.getInt("spawnZ", 0));
    }

    private MapType save(MapType worldData) {
        BlockPos spawnPoint = UltracraftServer.invokeAndWait(this::getSpawnPoint);
        worldData.putInt("playTime", this.playTime);
        worldData.putUUID("uid", this.uid);
        worldData.putInt("spawnX", spawnPoint.x());
        worldData.putInt("spawnY", spawnPoint.y());
        worldData.putInt("spawnZ", spawnPoint.z());
        worldData.putLong("seed", this.seed);

        ListType<MapType> recordedChanges = new ListType<>();
        for (RecordedChange change : this.recordedChanges) {
            recordedChanges.add(change.save());
        }

        worldData.put("RecordedChanges", recordedChanges);

        return worldData;
    }

    public static long getChunkUnloads() {
        return ServerWorld.chunkUnloads;
    }

    @Deprecated
    public static long getChunkLoads() {
        return 0;
    }

    @Deprecated
    public static long getChunkSaves() {
        return 0;
    }

    public int getChunksToLoad() {
        return this.chunksToLoadCount;
    }

    private <T> Queue<T> createSyncQueue() {
        return Queues.synchronizedQueue(Queues.newConcurrentLinkedQueue());
    }

    @Override
    public int getRenderDistance() {
        return this.server.getRenderDistance();
    }

    @Override
    protected boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkPos pos) {
        this.checkThread();

        if (!chunk.getPos().equals(pos)) {
            throw new ValidationError("Chunk position (" + chunk.getPos() + ") and provided position (" + pos + ") don't match.");
        }
        if (!this.unloadChunk(pos, true)) World.LOGGER.warn(World.MARKER, "Failed to unload chunk at {}", pos);

        WorldEvents.CHUNK_UNLOADED.factory().onChunkUnloaded(this, chunk.getPos(), chunk);
        return true;
    }

    /**
     * Sets the block at the given position
     *
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @param z     the z-coordinate
     * @param block the block type to set
     * @return true if the block was successfully set, false if setting the block failed
     * @see BlockFlags
     * @deprecated due to block setting changes, use {@link #set(int, int, int, BlockMetadata, int)} instead
     */
    @Override
    @Deprecated
    public boolean set(int x, int y, int z, @NotNull BlockMetadata block) {
        return set(x, y, z, block, BlockFlags.UPDATE | BlockFlags.SYNC);
    }

    /**
     * Sets the block at the given position.
     *
     * @param x     the x-coordinate of the block in world space
     * @param y     the y-coordinate of the block in world space
     * @param z     the z-coordinate of the block in world space
     * @param block the block type to set
     * @param flags the flags to use when setting the block. Values are defined in {@link BlockFlags}
     * @return true if the block was successfully set, false if setting the block failed
     * @see BlockFlags
     */
    @Override
    public boolean set(int x, int y, int z, @NotNull BlockMetadata block,
                       @MagicConstant(flagsFromClass = BlockFlags.class) int flags) {
        boolean isBlockSet = super.set(x, y, z, block, flags);
        BlockPos blockPos = new BlockPos(x, y, z);
        block.onPlace(this, blockPos);
        if (~(flags & BlockFlags.SYNC) != 0) this.sync(x, y, z, block);
        if (~(flags & BlockFlags.UPDATE) != 0) {
            for (CubicDirection direction : CubicDirection.values()) {
                BlockPos offset = blockPos.offset(direction);
                BlockMetadata blockMetadata = this.get(offset);
                blockMetadata.update(this, offset);
            }
        }

        return isBlockSet;
    }

    @Override
    public void setBlockEntity(@NotNull BlockPos pos, @NotNull BlockEntity blockEntity) {
        super.setBlockEntity(pos, blockEntity);

        if (this.getBlockEntity(pos) == blockEntity) {
            this.sendAllTracking(pos.x(), pos.y(), pos.z(), new S2CBlockEntitySetPacket(pos, blockEntity.getType().getRawId()));
        }
    }

    private void sync(int x, int y, int z, BlockMetadata block) {
        this.sendAllTracking(x, y, z, new S2CBlockSetPacket(new BlockPos(x, y, z), block));
    }

    public void sendAllTracking(int x, int y, int z, Packet<? extends ClientPacketHandler> packet) {
        for (var player : this.server.getPlayers()) {
            if (player.getWorld() != this) continue;

            if (player.isChunkActive(World.toChunkPos(x, y, z))) {
                player.connection.send(packet);
            }
        }
    }

    public void sendAllTrackingExcept(int x, int y, int z, Packet<? extends ClientPacketHandler> packet, @NotNull ServerPlayer except) {
        for (var player : this.server.getPlayers()) {
            if (player == except) continue;

            if (player.getWorld() != this) continue;

            if (player.isChunkActive(World.toChunkPos(x, y, z))) {
                player.connection.send(packet);
            }
        }
    }

    /**
     * Loads a chunk at the specified chunk position (x, z).
     *
     * @param pos The chunk position.
     * @return The loaded chunk or null if loading failed.
     */
    @Nullable
    @NonBlocking
    @SuppressWarnings("UnusedReturnValue")
    public Chunk loadChunk(ChunkPos pos) {
        // Ensure this method is called from the correct thread
        this.checkThread();

        // Load the chunk
        return this.loadChunk(pos.x(), pos.z());
    }

    /**
     * Loads a chunk at the specified global chunk position (x, z).
     *
     * @param x The x-coordinate of the chunk.
     * @param z The z-coordinate of the chunk.
     * @return The loaded chunk or null if loading failed.
     */
    @Nullable
    @NonBlocking
    public Chunk loadChunk(int x, int z) {
        // Ensure this method is called from the correct thread
        this.checkThread();

        // Load the chunk
        return this.loadChunk(x, z, false);
    }

    /**
     * Loads a chunk at the specified global chunk position (x, z), optionally overwriting an existing chunk.
     *
     * @param x         The x-coordinate of the chunk.
     * @param z         The z-coordinate of the chunk.
     * @param overwrite Whether to overwrite an existing chunk if present.
     * @return The loaded chunk or null if loading failed.
     * @throws IllegalChunkStateException If the chunk is already active.
     */
    @Nullable
    @NonBlocking
    public ServerChunk loadChunk(int x, int z, boolean overwrite) {
        // Ensure this method is called from the correct thread
        this.checkThread();

        // Calculate global and local chunk positions
        var globalPos = new ChunkPos(x, z);
        var localPos = World.toLocalChunkPos(x, z);

        try {
            // Check if there's an existing chunk at the global position
            var oldChunk = this.getChunk(globalPos);
            if (oldChunk != null && !overwrite) {
                return oldChunk;
            }

            // Get or open the region at the global position
            var region = this.getOrOpenRegionAt(globalPos);
            var chunk = region.openChunk(localPos, globalPos);

            // Return null if the chunk couldn't be opened
            if (chunk == null) {
                return null;
            }

            // Throw an error if the chunk is already active
            if (chunk.active) {
                throw new IllegalChunkStateException("Chunk is already active.");
            }

            // Trigger chunk loaded event and track chunk loads
            WorldEvents.CHUNK_LOADED.factory().onChunkLoaded(this, globalPos, chunk);
            ValueTracker.setChunkLoads(ValueTracker.getChunkLoads() + 1);

            return chunk;
        } catch (Exception e) {
            // Log and rethrow any exception that occurred during chunk loading
            World.LOGGER.error(World.MARKER, "Failed to load chunk " + globalPos + ":", e);
            throw e;
        }
    }

    /**
     * Loads a chunk at the specified global chunk position (x, z).
     *
     * @param pos The chunk position.
     * @return The loaded chunk or null if loading failed.
     */
    @Nullable
    @Blocking
    @SuppressWarnings("UnusedReturnValue")
    private Chunk loadChunkNow(ChunkPos pos) {
        // Ensure the method is called on the correct thread
        this.checkThread();

        // Load the chunk
        return this.loadChunkNow(pos.x(), pos.z());
    }

    /**
     * Loads a chunk at the specified global chunk position (x, z).
     *
     * @param x The x-coordinate of the chunk.
     * @param z The z-coordinate of the chunk.
     * @return The loaded chunk or null if loading failed.
     */
    @Nullable
    @Blocking
    public Chunk loadChunkNow(int x, int z) {
        // Ensure the method is called on the correct thread
        this.checkThread();

        // Load the chunk
        return this.loadChunkNow(x, z, false);
    }

    /**
     * Loads a chunk synchronously at the specified global chunk position (x, z), optionally overwriting an existing chunk.
     *
     * @param x         The x-coordinate of the chunk.
     * @param z         The z-coordinate of the chunk.
     * @param overwrite Whether to overwrite an existing chunk if found.
     * @return The loaded chunk or null if failed to load.
     */
    @Nullable
    @Blocking
    public ServerChunk loadChunkNow(int x, int z, boolean overwrite) {
        // Ensure the method is called on the correct thread
        this.checkThread();

        // Calculate global and local positions
        var globalPos = new ChunkPos(x, z);
        var localPos = World.toLocalChunkPos(x, z);

        try {
            // Check if there is an existing chunk at the global position
            var oldChunk = this.getChunk(globalPos);
            if (oldChunk != null && !overwrite) {
                return oldChunk;
            }

            // Get or open the region at the global position
            var region = this.getOrOpenRegionAt(globalPos);

            // Open the chunk at the local position within the region
            var chunk = region.openChunkNow(localPos, globalPos);
            if (chunk == null) {
                // Handle failure to load chunk
                this.server.handleChunkLoadFailure(globalPos, "Chunk not loaded on server.");
                World.LOGGER.warn("Failed to load chunk {}!", globalPos);
                return null;
            }
            if (chunk.active) {
                throw new IllegalChunkStateException("Chunk is already active.");
            }

            // Trigger CHUNK_LOADED event and update chunk load count
            WorldEvents.CHUNK_LOADED.factory().onChunkLoaded(this, globalPos, chunk);
            ValueTracker.setChunkLoads(ValueTracker.getChunkLoads() + 1);

            return chunk;
        } catch (Exception e) {
            // Log and rethrow any exceptions that occur during chunk loading
            World.LOGGER.error(World.MARKER, "Failed to load chunk " + globalPos + ":", e);
            throw e;
        }
    }

    public void tick() {
        this.playTime++;

        this.entities.removeIf(entity -> {
            if (entity.isMarkedForRemoval()) {
                this.entitiesById.remove(entity.getId());
                return true;
            }

            return false;
        });

        for (var entity : this.entitiesById.values()) {
            entity.tick();
        }

        var poll = this.tasks.poll();
        if (poll != null) poll.run();

        this.pollChunkQueues();
    }

    private void pollChunkQueues() {
        this.chunkLock.lock();
        try {
            this.server.profiler.section("chunkUnloads", () -> {
                var unload = this.chunksToUnload.poll();
                if (unload != null) {
                    var region = this.regionStorage.getRegionAt(unload);
                    if (region != null && region.getActiveChunk(World.toLocalChunkPos(unload)) != null) {
                        this.server.profiler.section("chunk[" + unload + "]", () -> this.unloadChunk(unload));
                    }
                }
            });
        } catch (Throwable t) {
            World.LOGGER.error("Failed to poll chunk task", t);
        }

        try {
            this.server.profiler.section("chunkLoads", () -> {
                var load = this.chunksToLoad.poll();
                if (load != null) {
                    this.server.profiler.section("chunk[" + load + "]", () -> this.loadChunk(load));
                }
            });
        } catch (Throwable t) {
            World.LOGGER.error("Failed to poll chunk task", t);
        }
        this.chunkLock.unlock();
    }

    /**
     * Loads/unloads chunks requested by the given refresher.
     * Note: Internal API: Do not call when you don't know what you are doing.
     *
     * @param refresher The refresher that requested the chunks.
     */
    @ApiStatus.Internal
    public void doRefresh(ChunkRefresher refresher) {
        if (!refresher.isFrozen()) return;
        if (!this.chunksToLoad.isEmpty()) return;
        if (!this.chunksToUnload.isEmpty()) return;

        if (!this.server.isInitialChunksLoaded()) {
            if (this.chunksToLoadCount > 0) {
                this.chunksToLoadCount = -1;
                this.server.onInitialChunksLoaded();
            }
            if (this.chunksToLoadCount == 0) {
                this.chunksToLoadCount = refresher.toLoad.size();
            }

            return;
        }

        for (ChunkPos pos : refresher.toLoad) {
            this.deferLoadChunk(pos);
        }

        for (ChunkPos pos : refresher.toUnload) {
            this.deferUnloadChunk(pos);
        }
    }

    @ApiStatus.Internal
    public void doRefreshNow(ChunkRefresher refresher) {
        for (ChunkPos pos : refresher.toLoad) {
            this.loadChunkNow(pos);
        }

        for (ChunkPos pos : refresher.toUnload) {
            this.deferUnloadChunk(pos);
        }
    }

    @Deprecated
    public void refreshChunks(float x, float z) {
        this.refreshChunks(new Vec3d(x, World.WORLD_DEPTH, z));
    }

    @Deprecated
    public void refreshChunks(Vec3d ignoredVec) {

    }

    @Deprecated
    public void refreshChunks(Player ignoredPlayer) {

    }

    private void deferLoadChunk(ChunkPos chunkPos) {
        this.chunkLock.lock();
        try {
            if (this.chunksToLoad.contains(chunkPos)) return;
            this.chunksToLoad.offer(chunkPos);
        } catch (Throwable t) {
            World.LOGGER.error("Failed to defer chunk " + chunkPos + ":", t);
        }
        this.chunkLock.unlock();
    }

    private void deferUnloadChunk(ChunkPos chunkPos) {
        this.chunkLock.lock();
        try {
            if (this.chunksToUnload.contains(chunkPos)) return;
            this.chunksToUnload.add(chunkPos);
        } catch (Throwable t) {
            World.LOGGER.error("Failed to defer chunk " + chunkPos + ":", t);
        }
        this.chunkLock.unlock();
    }

    /**
     * Unloads a chunk from the world.
     *
     * @param chunkPos     the position of the chunk to unload
     * @param save         if true, the chunk will be saved to disk.
     * @param ignoredForce deprecated.
     * @return true if the chunk was successfully unloaded.
     * @deprecated use {@link #unloadChunk(ChunkPos, boolean)} instead.
     */
    @Deprecated
    public boolean unloadChunk(ChunkPos chunkPos, boolean save, @Deprecated boolean ignoredForce) {
        return this.unloadChunk(chunkPos, save);
    }

    /**
     * Unloads a chunk from the world.
     *
     * @param chunkPos the position of the chunk to unload
     * @param save     if true, the chunk will be saved to disk.
     * @return true if the chunk was successfully unloaded.
     */
    public boolean unloadChunk(ChunkPos chunkPos, boolean save) {
        this.checkThread();

        if (shouldStayLoaded(chunkPos)) return false;

        var region = this.regionStorage.getRegionAt(chunkPos);
        if (region == null) throw new IllegalStateException("Region is unloaded while unloading chunk " + chunkPos);

        var localChunkPos = World.toLocalChunkPos(chunkPos);

        if (region.getActiveChunk(localChunkPos) == null)
            throw new IllegalChunkStateException("Tried to unload chunk %s but it isn't active".formatted(chunkPos));

        var chunk = region.deactivate(localChunkPos);
        if (chunk == null) {
            throw new IllegalChunkStateException("Tried to unload non-existing chunk: " + chunkPos);
        }

        if (region.isEmpty() && save) {
            try {
                this.saveRegion(region);
            } catch (Exception e) {
                World.LOGGER.warn("Failed to save region %s:".formatted(region.getPos()), e);
                return false;
            }
        }

        ServerWorld.chunkUnloads++;
        return true;
    }

    public int getPlayTime() {
        return this.playTime;
    }

    /**
     * Check if the current thread is the server thread.
     *
     * @throws InvalidThreadException if the current thread is not the server thread.
     */
    @Override
    protected void checkThread() {
        if (!UltracraftServer.isOnServerThread()) throw new InvalidThreadException("Should be on server thread.");
    }

    /**
     * Retrieves the chunk at the given position.
     *
     * @param pos The position of the chunk.
     * @return The chunk at the given position, or null if not found.
     */
    @Override
    public @Nullable ServerChunk getChunk(@NotNull ChunkPos pos) {
        // Get the region at the specified position
        var region = this.regionStorage.getRegionAt(pos);

        // If the region is not found, return null
        if (region == null) return null;

        // Convert the chunk position to local coordinates
        var localPos = World.toLocalChunkPos(pos);

        // Get the chunk from the region
        var chunk = region.getChunk(localPos);

        // If the chunk is found, verify its position matches the expected position
        if (chunk != null) {
            ChunkPos foundAt = chunk.getPos();

            // If the positions don't match, throw a validation error
            if (!foundAt.equals(localPos)) {
                throw new ValidationError("Chunk expected to be found at %s was found at %s instead.".formatted(pos, foundAt));
            }
        }

        // Return the chunk
        return chunk;
    }

    /**
     * Get all chunks currently loaded in.
     *
     * @return the currently loaded chunks.
     */
    @Override
    public Collection<ServerChunk> getLoadedChunks() {
        var regions = this.regionStorage.regions.values();
        return regions.stream().flatMap(value -> value.getChunks().stream()).toList();
    }

    /**
     * Disposes the world, and cleans up the objects it uses.
     * Added for clean closing of the world.
     */
    @Override
    @ApiStatus.Internal
    public void dispose() {
        this.disposed = true;
        var saveSchedule = this.saveSchedule;
        if (saveSchedule != null) saveSchedule.cancel(true);
        this.saveExecutor.shutdownNow();
        super.dispose();

        this.regionStorage.dispose();

        this.terrainGen.dispose();
    }

    @Override
    public int getTotalChunks() {
        return this.regionStorage.getChunkCount();
    }

    @Override
    public BreakResult continueBreaking(@NotNull BlockPos breaking, float amount, @NotNull Player breaker) {
        return super.continueBreaking(breaking, amount, breaker);
    }

    @Override
    public BlockPos getSpawnPoint() {
        ChunkPos chunkPos = World.toChunkPos(this.spawnX, 0, this.spawnZ);
        Chunk chunk = this.getChunk(chunkPos);
        if (chunk == null) {
            chunk = this.loadChunkNow(chunkPos);
            if (chunk == null)
                throw new IllegalStateException("Failed to load chunk at spawn position");
        }

        BlockPos localPos = World.toLocalBlockPos(this.spawnX, 0, this.spawnZ);
        int highest = chunk.getHighest(localPos.x(), localPos.z());
        int spawnY = 64;
        if (highest != Integer.MIN_VALUE)
            spawnY = highest;

        return new BlockPos(this.spawnX, spawnY + 1, this.spawnZ);
    }

    /**
     * Play a sound at a specific position.
     *
     * @param sound sound to play.
     * @param x     the X-position.
     * @param y     the Y-position.
     * @param z     the Z-position.
     */
    @Override
    public void playSound(@NotNull SoundEvent sound, double x, double y, double z) {
        float range = sound.getRange();
        var playersWithinRange = this.getPlayersWithinRange(x, y, z, range);
        for (Player player : playersWithinRange) {
            player.playSound(sound, (float) ((range - player.getPosition().dst(x, y, z)) / range));
        }
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    /**
     * @param x     the origin X-position.
     * @param y     the origin Y-position.
     * @param z     the origin Z-position.
     * @param range the chance.
     * @return the players within the chance of the XYZ coordinates.
     */
    public List<ServerPlayer> getPlayersWithinRange(double x, double y, double z, float range) {
        var withinRange = new ArrayList<ServerPlayer>();

        // Assuming you have a list of Player objects with their coordinates
        var allPlayers = this.server.getPlayers();

        for (var player : allPlayers) {
            var playerX = player.getX();
            var playerY = player.getY();
            var playerZ = player.getZ();

            // Calculate the distance between the given point (x, y, z) and the player's coordinates
            var distance = Math.sqrt(Math.pow(playerX - x, 2) + Math.pow(playerY - y, 2) + Math.pow(playerZ - z, 2));

            // Check if the distance is within the specified chance
            if (distance <= range) {
                withinRange.add(player);
            }
        }

        return withinRange;
    }

    /**
     * @return the storage for world data.
     */
    public WorldStorage getStorage() {
        return this.storage;
    }

    /**
     * Loads the world from the disk.
     *
     * @throws IOException when world loading fails.
     */
    @Blocking
    @ApiStatus.Internal
    public void load() throws IOException {
        this.storage.createDir("players/");
        this.storage.createDir("regions/");

        World.LOGGER.info(World.MARKER, "Loading world: " + this.storage.getDirectory().getFileName());

        //<editor-fold defaultstate="collapsed" desc="<<Loading: entities.ubo>>">
        if (this.storage.exists("entities.ubo")) {
            ListType<MapType> entityListData = this.storage.read("entities.ubo");
            for (var entityData : entityListData.getValue()) {
                var entity = Entity.loadFrom(this, entityData);
                this.entitiesById.put(entity.getId(), entity);
            }
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="<<Loading: world.ubo>>">
        if (this.storage.exists("world.ubo")) {
            MapType worldData = this.storage.read("world.ubo");
            this.load(worldData);
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="<<Starting: Auto Save Schedule>>">
        this.saveSchedule = this.server.schedule(new Task<>(new Identifier("auto_save")) {
            @Override
            public void run() {
                try {
                    ServerWorld.this.save(true);
                } catch (Exception e) {
                    World.LOGGER.error(World.MARKER, "Failed to save world:", e);
                }
                ServerWorld.this.saveSchedule = ServerWorld.this.server.schedule(this, UltracraftServerConfig.autoSaveInterval, TimeUnit.SECONDS);
            }
        }, UltracraftServerConfig.initialAutoSaveDelay, TimeUnit.SECONDS);
        //</editor-fold>

        WorldEvents.LOAD_WORLD.factory().onLoadWorld(this, this.storage);

        World.LOGGER.info(World.MARKER, "Loaded world: " + this.storage.getDirectory().getFileName());
    }

    /**
     * Save the world to disk.
     *
     * @param silent true to silence the logs.
     * @throws IOException when saving the world fails.
     */
    @Blocking
    @ApiStatus.Internal
    public synchronized void save(boolean silent) throws IOException {
        if (this.saving) return;
        this.saving = true;

        // Log saving world message if not silent
        if (!silent) World.LOGGER.info(World.MARKER, "Saving world: " + this.storage.getDirectory().getFileName());

        // Save entities data
        var entitiesData = new ListType<MapType>();
        for (var entity : this.entitiesById.values()) {
            if (entity instanceof Player) continue;

            var entityData = entity.save(new MapType());
            entitiesData.add(entityData);
        }
        this.storage.write(entitiesData, "entities.ubo");

        // Save world data
        MapType save = this.save(new MapType());
        this.storage.write(save, "world.ubo");

        // Save regions data
        for (var region : this.regionStorage.regions.values()) {
            try {
                if (region.isDirty())
                    this.saveRegion(region, false);
            } catch (Exception e) {
                World.LOGGER.warn("Failed to save region %s:".formatted(region.getPos()), e);
                var remove = this.regionStorage.regions.remove(region.getPos());
                if (remove != region)
                    this.server.crash(new ValidationError("Removed region is not the region that got saved."));
                region.dispose();
            }
        }

        // Trigger save world event
        WorldEvents.SAVE_WORLD.factory().onSaveWorld(this, this.storage);

        // Log saved world message if not silent
        if (!silent) World.LOGGER.info(World.MARKER, "Saved world: " + this.storage.getDirectory().getFileName());
        this.saving = false;
    }

    /**
     * Save a region to disk.
     *
     * @param region the region to save.
     */
    @Blocking
    public void saveRegion(Region region) {
        this.saveRegion(region, true);
    }

    /**
     * Save a region to disk.
     *
     * @param region  the region to save.
     * @param dispose true to also dispose the region.
     */
    @Blocking
    public void saveRegion(Region region, boolean dispose) {
        var file = this.storage.regionFile(region.getPos());
        try (var stream = new GZIPOutputStream(new FileOutputStream(file, false), true)) {
            var dataStream = new DataOutputStream(stream);

            region.writeLock();
            this.regionStorage.save(region, dataStream, dispose);
            region.writeUnlock();
            if (!region.dirtyWhileSaving) region.dirty = false;
            else region.dirtyWhileSaving = false;
        } catch (IOException e) {
            World.LOGGER.error("Failed to save region %s".formatted(region.getPos()), e);
        }
    }

    /**
     * Internal asynchronous save method.
     *
     * @param silent true to silence the logs.
     * @return the future of the save function. Will return true if successful when finished.
     */
    @ApiStatus.Internal
    @NonBlocking
    public CompletableFuture<Boolean> saveAsync(boolean silent) {
        // Check if there is a save schedule running
        var saveSchedule = this.saveSchedule;

        // If there is a save schedule running and it's not done, return the existing saveFuture if available
        if (saveSchedule != null && !saveSchedule.isDone()) {
            return this.saveFuture != null ? this.saveFuture : CompletableFuture.completedFuture(true);
        }

        // If there is a save schedule running, cancel it
        if (saveSchedule != null) {
            saveSchedule.cancel(false);
        }

        try {
            // Run the save operation asynchronously
            return this.saveFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    this.save(silent);
                    return true;
                } catch (Exception e) {
                    // Handle save error
                    this.server.handleWorldSaveError(e);
                    World.LOGGER.error(World.MARKER, "Failed to save world", e);
                    return false;
                }
            }, this.saveExecutor);
        } catch (Exception e) {
            // Log error if save operation fails
            World.LOGGER.error(World.MARKER, "Failed to save world", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Gets or opens the region at the specified chunk position.
     *
     * @param chunkPos the chunk position to get or open the region at
     * @return the region at the specified chunk position
     */
    @NotNull
    private Region getOrOpenRegionAt(ChunkPos chunkPos) {
        // Ensure this method is called from the correct thread
        this.checkThread();

        // Calculate region coordinates
        var rx = chunkPos.x() / World.REGION_SIZE;
        var rz = chunkPos.z() / World.REGION_SIZE;

        // Create a new region position
        var regionPos = new RegionPos(rx, rz);

        // Get the map of regions
        var regions = this.regionStorage.regions;

        // Check if the region already exists at the calculated position
        var oldRegion = regions.get(regionPos);
        if (oldRegion != null) {
            return oldRegion;
        }

        // If the region does not exist, try to open it
        try {
            if (this.storage.regionExists(rx, rz)) {
                return this.openRegion(rx, rz);
            }
        } catch (IOException e) {
            // Log error if the region failed to load
            World.LOGGER.error("Region at %s,%s failed to load:".formatted(rx, rz), e);
        }

        // Create a new region if it doesn't exist and add it to the regions map
        var region = new Region(this, regionPos);
        this.regionStorage.regions.put(regionPos, region);
        this.regionStorage.chunkCount += region.getChunkCount();

        return region;
    }

    /**
     * Opens and loads a region from the specified coordinates.
     *
     * @param rx The x-coordinate of the region.
     * @param rz The z-coordinate of the region.
     * @return The loaded region.
     * @throws IOException If an I/O error occurs.
     */
    @NotNull
    private Region openRegion(int rx, int rz) throws IOException {
        var fileHandle = this.storage.regionFile(rx, rz);
        try (var stream = new GZIPInputStream(new FileInputStream(fileHandle))) {
            var dataStream = new DataInputStream(stream);
            return this.regionStorage.load(this, dataStream);
        }
    }

    /**
     * Spawn an entity into the world.
     *
     * @param entity the entity to spawn.
     * @param <T>    the entity.
     * @return the spawned entity. (Same as {@code entity} parameter)
     */
    @Override
    public <T extends Entity> T spawn(@NotNull T entity) {
        if (!(entity instanceof ServerPlayer) && entity instanceof Player)
            throw new IllegalStateException("Tried to spawn a non-server player in a server world.");

        T spawn = super.spawn(entity);

        if (entity instanceof ServerPlayer player)
            sendAllTracking(spawn.getBlockPos().x(), spawn.getBlockPos().y(), spawn.getBlockPos().z(), new S2CAddPlayerPacket(player.getUuid(), player.getName(), new Vec3d(spawn.getBlockPos().x() + 0.5, spawn.getBlockPos().y(), spawn.getBlockPos().z() + 0.5)));
        else
            sendAllTracking(spawn.getBlockPos().x(), spawn.getBlockPos().y(), spawn.getBlockPos().z(), new S2CAddEntityPacket(spawn));

        return spawn;
    }

    /**
     * Prepares spawn for a player.<br>
     * This method is synchronous with the server thread.<br>
     * The action will block the thread.
     *
     * @param player the player to prepare the spawn for.
     */
    public void prepareSpawn(Player player) {
        if (!UltracraftServer.isOnServerThread()) {
            UltracraftServer.invokeAndWait(() -> this.prepareSpawn(player));
            return;
        }

        ChunkPos origin = player.getChunkPos();
        for (int x = origin.x() - 1; x <= origin.x() + 1; x++) {
            for (int z = origin.z() - 1; z <= origin.z() + 1; z++) {
                this.loadChunk(x, z);
            }
        }
    }

    /**
     * @return the server this world belongs to.
     */
    public UltracraftServer getServer() {
        return this.server;
    }

    /**
     * Should only be used for debugging.<br>
     * Note that this method is not thread safe.<br>
     * Only run this method in the server thread.<br>
     * Be sure to remove the client chunk before calling this.
     *
     * @param globalPos the position of the chunk in global space.
     */
    @ApiStatus.Experimental
    public void regenerateChunk(ChunkPos globalPos) {
        this.checkThread();
        this.unloadChunk(globalPos);
        Region region = this.getOrOpenRegionAt(globalPos);
        var localPos = World.toLocalChunkPos(globalPos);
        region.activeChunks.remove(localPos);
        region.chunks.remove(localPos);
        region.chunkCount--;
        regionStorage.chunkCount--;
        region.generateChunk(localPos, globalPos);
    }

    /**
     * Sets the spawn point for the world.<br>
     * The spawn point is set randomly.
     */
    public void setupSpawn() {
        int spawnChunkX = MathUtils.random(-32, 31);
        int spawnChunkZ = MathUtils.random(-32, 31);
        int spawnX = MathUtils.random(spawnChunkX * 16, spawnChunkX * 16 + 15);
        int spawnZ = MathUtils.random(spawnChunkZ * 16, spawnChunkZ * 16 + 15);

        this.setSpawnPoint(spawnX, spawnZ);
    }

    public void recordOutOfBounds(int x, int y, int z, BlockMetadata block) {
        if (!(UltracraftServer.isOnServerThread())) {
            UltracraftServer.invokeAndWait(() -> this.recordOutOfBounds(x, y, z, block));
            return;
        }
        if (this.isOutOfWorldBounds(x, y, z)) {
            return;
        }

        Chunk chunkAt = this.getChunkAt(x, y, z);
        if (chunkAt == null) {
            if (WorldGenDebugContext.isActive())
                System.out.println("[DEBUG] Recorded out of bounds block at " + x + " " + y + " " + z + " " + block);
            this.recordedChanges.add(new RecordedChange(x, y, z, block));
            return;
        }

        if (WorldGenDebugContext.isActive())
            System.out.println("[DEBUG] Chunk is available, setting block at " + x + " " + y + " " + z + " " + block);

        chunkAt.setFast(World.toLocalBlockPos(x, y, z).vec(), block);
    }

    /**
     * Retrieve the TerrainGenerator object.
     *
     * @return the TerrainGenerator object
     */
    public TerrainGenerator getTerrainGenerator() {
        return this.terrainGen;
    }

    /**
     * Update the block at the given position.
     *
     * @param pos the position of the block
     */
    public void sync(Vec3i pos) {
        this.sync(pos.x, pos.y, pos.z, this.get(pos.x, pos.y, pos.z));
    }

    /**
     * Update the block at the given position.
     *
     * @param pos the position of the block
     */
    public void sync(BlockPos pos) {
        this.sync(pos.x(), pos.y(), pos.z(), this.get(pos));
    }

    /**
     * Check if the chunk at the given position is loaded.
     *
     * @param blockPos the position of the block
     * @return true if the chunk is loaded
     */
    public boolean isLoaded(BlockPos blockPos) {
        return this.getChunkAt(blockPos) != null;
    }

    /**
     * Check if the chunk at the given position is loaded.
     *
     * @param chunkPos the position of the chunk
     * @return true if the chunk is loaded
     */
    public boolean isLoaded(ChunkPos chunkPos) {
        return this.getChunk(chunkPos) != null;
    }

    /**
     * The region class.
     * Note: This class is not thread safe.
     *
     * @author <a href="https://github.com/XyperCode">XyperCode</a>
     * @since 0.1.0
     */
    @NotThreadSafe
    public static class Region implements ServerDisposable {
        private final Set<ChunkPos> activeChunks = new CopyOnWriteArraySet<>();
        private final RegionPos pos;
        public int dataVersion;
        public String lastPlayedIn = UltracraftServer.get().getGameVersion();
        public boolean saving;
        public boolean dirtyWhileSaving;
        private Map<ChunkPos, ServerChunk> chunks = Object2ObjectMaps.synchronize(new Object2ObjectArrayMap<>());
        private boolean disposed = false;
        private final ServerWorld world;
        private final List<ChunkPos> generatingChunks = new CopyOnWriteArrayList<>();
        private final Object buildLock = new Object();
        private boolean dirty;
        private final Lock writeLock = new ReentrantLock(true);
        private int chunkCount;

        /**
         * Constructs a new region with the given world and position.
         *
         * @param world the world this region belongs to.
         * @param pos   the position of the region.
         */
        public Region(ServerWorld world, RegionPos pos) {
            this.world = world;
            this.pos = pos;
        }

        /**
         * Constructs a new region with the given world and position. It also preloads all chunks.
         *
         * @param world  the world this region belongs to.
         * @param pos    the position of the region.
         * @param chunks the chunks to load into the region.
         */
        public Region(ServerWorld world, RegionPos pos, Map<ChunkPos, ServerChunk> chunks) {
            this.world = world;
            this.pos = pos;
            this.chunks = chunks;
        }

        /**
         * @return all loaded chunks within the region.
         */
        public Collection<ServerChunk> getChunks() {
            return List.copyOf(this.chunks.values());
        }

        /**
         * @return the position of the region.
         */
        public RegionPos pos() {
            return this.getPos();
        }

        /**
         * Dispose the region, clearing up chunk data for the garbage collector to free up memory.
         * Note: Internal API.
         * Should only be called if you know what you are doing.
         */
        @Override
        @ApiStatus.Internal
        public void dispose() {
            this.validateThread();
            if (this.disposed) return;
            this.disposed = true;

            for (Chunk value : this.chunks.values()) {
                value.dispose();
            }

            this.chunks.clear();
        }

        /**
         * Deactivates the chunk at the given position.
         * The chunk will still be loaded in memory for reuse.
         *
         * @param chunkPos the local position of the chunk to deactivate.
         * @return the deactivated chunk, or null if the chunk wasn't loaded.
         */
        @Nullable
        public Chunk deactivate(@NotNull ChunkPos chunkPos) {
            this.validateLocalPos(chunkPos);
            this.validateThread();
            Chunk chunk = this.chunks.get(chunkPos);

            if (chunk == null) return null;

            if (!this.activeChunks.remove(chunkPos))
                throw new IllegalChunkStateException("Can't deactivate an already inactive chunk.");

            chunk.active = false;
            return chunk;
        }

        /**
         * Activate the chunk at the given position.
         *
         * @param chunkPos  the local position of the chunk to activate.
         * @param globalPos the global position of the chunk to activate.
         * @return the acivated chunk, or null if the chunk wasn't loaded.
         */
        @Nullable
        @CanIgnoreReturnValue
        public Chunk activate(@NotNull ChunkPos chunkPos, ChunkPos globalPos) {
            this.validateLocalPos(chunkPos);
            this.validateThread();

            @Nullable Chunk chunk = this.chunks.get(chunkPos);

            if (chunk == null) return null;

            if (this.activeChunks.contains(chunkPos)) return chunk;

            this.activeChunks.add(chunkPos);
            chunk.active = true;

            return chunk;
        }

        private void validateLocalPos(ChunkPos chunkPos) {
            Preconditions.checkElementIndex(chunkPos.x(), World.REGION_SIZE, "Chunk x-position out of chance");
            Preconditions.checkElementIndex(chunkPos.z(), World.REGION_SIZE, "Chunk z-position out of chance");
        }

        /**
         * Get a currently loaded chunk with the specified local position.
         *
         * @param chunkPos the position of the chunk to get.
         * @return the chunk at that position, or null if the chunk wasn't loaded.
         */
        @Nullable
        public ServerChunk getChunk(ChunkPos chunkPos) {
            this.validateLocalPos(chunkPos);
            this.validateThread();
            return this.chunks.get(chunkPos);
        }

        /**
         * Get the currently active chunks in this section.
         *
         * @return the active chunks.
         */
        public Set<ChunkPos> getActiveChunks() {
            return Collections.unmodifiableSet(this.activeChunks);
        }

        /**
         * Get the active chunk at the specified position;
         *
         * @param pos the position of the active chunk to get.
         * @return the active chunk, or null if there's no active chunk at the specified position.
         */
        @Nullable
        public Chunk getActiveChunk(ChunkPos pos) {
            this.validateLocalPos(pos);
            this.validateThread();
            if (!this.activeChunks.contains(pos)) {
                return null;
            }
            return this.chunks.get(pos);
        }

        /**
         * @return true if this region has active chunks.
         */
        public boolean hasActiveChunks() {
            return !this.activeChunks.isEmpty();
        }

        /**
         * @return true if there are no active chunks.
         */
        public boolean isEmpty() {
            return this.activeChunks.isEmpty();
        }

        @CanIgnoreReturnValue
        public Chunk putChunk(@NotNull ChunkPos pos, @NotNull ServerChunk chunk) {
            this.validateLocalPos(pos);
            this.validateThread();

            if (this.chunks.containsKey(pos)) throw new IllegalChunkStateException("Chunk is already loaded");

            ServerChunk put = this.chunks.put(pos, chunk);
            world.regionStorage.chunkCount++;
            this.chunkCount++;
            return put;
        }

        private void validateThread() {
            if (!UltracraftServer.isOnServerThread()) {
                throw new InvalidThreadException("Should be on server thread.");
            }
        }

        /**
         * Generate a chunk at the specified chunk position local to the region.
         *
         * @param pos       the position of the chunk to generate.
         * @param globalPos the global position of the chunk to generate.
         */
        @NewInstance
        public void generateChunk(ChunkPos pos, ChunkPos globalPos) {
            this.validateThread();

            this.buildChunkAsync(globalPos);
        }

        /**
         * Generate a chunk at the specified chunk position local to the region.
         *
         * @param pos       the position of the chunk to generate.
         * @param globalPos the global position of the chunk to generate.
         * @return the generated chunk.
         */
        @NewInstance
        public ServerChunk generateChunkNow(ChunkPos pos, ChunkPos globalPos) {
            this.validateThread();

            // Add the chunk to the list of generating chunks.
            // Will return immediately if the chunk is already being built.
            synchronized (this.buildLock) {
                if (this.generatingChunks.contains(globalPos)) return null;
                this.generatingChunks.add(globalPos);
            }

            var ref = new Object() {
                ServerChunk builtChunk = null;
            };
            try {
                ref.builtChunk = this.buildChunk(globalPos);
            } catch (CancellationException e) {
                this.generatingChunks.remove(globalPos);
                return null;
            } catch (Throwable t) {
                this.generatingChunks.remove(globalPos);
                World.LOGGER.error("Failed to build chunk at %s:".formatted(globalPos), t);
                throw new Error(t);
            }

            var players = this.world.getServer().getPlayersInChunk(globalPos);
            players.forEach(player -> {
                try {
                    BlockPos globalTpPos = player.getBlockPos();
                    BlockPos localTpPos = World.toLocalBlockPos(globalTpPos);
                    int x = localTpPos.x();
                    int z = localTpPos.z();
                    player.teleportTo(globalTpPos.x(), ref.builtChunk.ascend(x, (int) player.getY(), z), globalTpPos.z());
                } catch (Exception e) {
                    World.LOGGER.error("Failed to teleport player outside unloaded chunk:", e);
                }
            });

            return ref.builtChunk;
        }

        /**
         * Build the chunk at the specified chunk position local to the region.
         * Note: this method is asynchronous.
         *
         * @param globalPos the global position of the chunk to generate.
         */
        private void buildChunkAsync(ChunkPos globalPos) {
            if (UltracraftServerConfig.debugWarnChunkBuildOverload && this.world.executor.getActiveCount() == this.world.executor.getMaximumPoolSize()) {
                World.LOGGER.warn("Chunk building is being overloaded!");
            }

            // Add the chunk to the list of generating chunks.
            // Will return immediately if the chunk is already being built.
            synchronized (this.buildLock) {
                if (this.generatingChunks.contains(globalPos)) return;
                this.generatingChunks.add(globalPos);
            }

            // Build the chunk asynchronously.
            CompletableFuture.supplyAsync(() -> {
                try {
                    return this.buildChunk(globalPos);
                } catch (CancellationException | RejectedExecutionException e) {
                    throw e;
                } catch (Throwable e) {
                    UltracraftServer.LOGGER.error("Failed to build chunk at %s:".formatted(globalPos), e);
                    throw new Error(e);
                }
            }, this.world.executor).thenAccept(builtChunk -> UltracraftServer.invoke(() -> {
                var players = this.world.getServer().getPlayersInChunk(globalPos);
                players.forEach(player -> {
                    try {
                        BlockPos globalTpPos = player.getBlockPos();
                        BlockPos localTpPos = World.toLocalBlockPos(globalTpPos);
                        int x = localTpPos.x();
                        int z = localTpPos.z();
                        player.teleportTo(globalTpPos.x(), builtChunk.ascend(x, (int) player.getY(), z), globalTpPos.z());
                    } catch (Exception e) {
                        World.LOGGER.error("Failed to teleport player outside unloaded chunk:", e);
                    }
                });
            }).exceptionallyAsync(e -> {
                if (!(e instanceof CancellationException))
                    UltracraftServer.LOGGER.error("Failed to build chunk at %s:".formatted(globalPos), e);

                return null;
            }));
        }

        @CheckReturnValue
        private ServerChunk buildChunk(ChunkPos globalPos) {
            var localPos = World.toLocalChunkPos(globalPos);
            var chunk = new BuilderChunk(this.world, Thread.currentThread(), globalPos, this);

            // Generate terrain using the terrain generator.
            this.world.terrainGen.generate(chunk, List.copyOf(world.recordedChanges));

            WorldEvents.CHUNK_BUILT.factory().onChunkGenerated(this.world, this, chunk);

            // Put the chunk into the list of loaded chunks.
            ServerChunk builtChunk = chunk.build();
            this.chunks.put(localPos, builtChunk);
            this.world.regionStorage.chunkCount++;
            this.chunkCount++;

            // Send the chunk to all connections.
            CompletableFuture.runAsync(() -> {
                try {
                    this.world.server.sendChunk(globalPos, builtChunk);
                } catch (CancellationException e) {
                    this.generatingChunks.remove(globalPos);
                } catch (IOException e) {
                    this.generatingChunks.remove(globalPos);
                    throw new RuntimeException(e);
                }
            });

            // Mark the chunk as ready.
            builtChunk.ready = true;

            // Chunk isn't generating anymore.
            this.generatingChunks.remove(globalPos);

            return builtChunk;
        }

        /**
         * Opens a chunk at a specified position. If it isn't loaded yet, it will defer generating the chunk.
         *
         * @param pos       the region local position of the chunk to open.
         * @param globalPos the global position.
         * @return the loaded/generated chunk.
         */
        public @Nullable ServerChunk openChunk(ChunkPos pos, ChunkPos globalPos) {
            this.validateThread();

            @Nullable ServerChunk loadedChunk = this.getChunk(pos);
            if (loadedChunk == null) {
                this.generateChunk(pos, globalPos);
                return null;
            }

            var loadedAt = loadedChunk.getPos();
            if (!loadedAt.equals(pos)) {
                throw new IllegalChunkStateException("Chunk requested to load at %s got loaded at %s instead".formatted(pos, loadedAt));
            }

            return loadedChunk;
        }

        /**
         * Opens a chunk at a specified position. If it isn't loaded yet, it will defer generating the chunk.
         *
         * @param pos       the region local position of the chunk to open.
         * @param globalPos the global position.
         * @return the loaded/generated chunk.
         */
        public @Nullable ServerChunk openChunkNow(ChunkPos pos, ChunkPos globalPos) {
            this.validateThread();

            @Nullable ServerChunk loadedChunk = this.getChunk(pos);
            if (loadedChunk == null) {
                return this.generateChunkNow(pos, globalPos);
            }

            var loadedAt = loadedChunk.getPos();
            if (!loadedAt.equals(pos)) {
                throw new IllegalChunkStateException("Chunk requested to load at %s got loaded at %s instead".formatted(pos, loadedAt));
            }

            return loadedChunk;
        }

        public RegionPos getPos() {
            return this.pos;
        }

        public boolean isDirty() {
            return dirty;
        }

        public void markDirty() {
            if (saving) this.dirtyWhileSaving = true;
            this.dirty = true;
        }

        public <T> Result<T> trySet(Supplier<T> supplier) {
            if (!this.writeLock.tryLock()) {
                return Result.failure(new IllegalStateException("Cannot acquire write lock"));
            }

            try {
                return Result.ok(supplier.get());
            } finally {
                this.writeLock.unlock();
            }
        }

        public void writeLock() {
            this.writeLock.lock();
        }

        public void writeUnlock() {
            this.writeLock.unlock();
        }

        public int getChunkCount() {
            return this.chunkCount;
        }
    }

    /**
     * Represents a collection of regions.
     *
     * @author <a href="https://github.com/XyperCode">XyperCode</a>
     * @since 0.1.0
     */
    public static class RegionStorage {
        private final Map<RegionPos, Region> regions = new ConcurrentHashMap<>();
        private int chunkCount;

        /**
         * Saves a region to an output stream.
         *
         * @param region  the region to save.
         * @param stream  the output stream to save to.
         * @param dispose if true, the region will be disposed after saving.
         * @throws IOException if an I/O error occurs.
         */
        @ApiStatus.Internal
        public void save(Region region, DataOutputStream stream, boolean dispose) throws IOException {
            var pos = region.pos();

            MapType mapType = new MapType();
            mapType.putInt("dataVersion", region.dataVersion);
            mapType.putString("lastPlayedIn", region.lastPlayedIn);
            mapType.putInt("x", pos.x());
            mapType.putInt("z", pos.z());
            mapType.putInt("size", World.REGION_SIZE);

            // Write chunks to the region file.
            var chunks = region.getChunks().stream().filter(serverChunk -> !serverChunk.isOriginal()).toList();
            var idx = 0;
            CommonConstants.LOGGER.info("Saving " + chunks.size() + " chunks in region " + pos);
            for (var chunk : chunks) {
                if (idx >= World.REGION_SIZE * World.REGION_SIZE)
                    throw new IllegalArgumentException("Too many chunks in region!");
                if (chunk.isOriginal()) continue;
                CommonConstants.LOGGER.info("Saving chunk " + chunk.getPos() + " in region " + pos);
                var localChunkPos = World.toLocalChunkPos(chunk.getPos());
                mapType.put("c" + localChunkPos.x() + ";" + localChunkPos.z(), chunk.save());
                idx++;
            }

            // Write region metadata.
            DataIo.write(mapType, stream);

            // Dispose the region if requested.
            if (dispose) {
                this.regions.remove(region.getPos());
                this.chunkCount -= region.getChunkCount();
                UltracraftServer.invokeAndWait(region::dispose);
            }
        }

        /**
         * Loads a region from an input stream.
         *
         * @param world  the world to load the region in.
         * @param stream the input stream to load from.
         * @return the loaded region.
         * @throws IOException if an I/O error occurs.
         */
        public Region load(ServerWorld world, DataInputStream stream) throws IOException {
            // Read region metadata.
            MapType read = DataIo.read(stream);
            int dataVersion = read.getInt("dataVersion");
            String lastPlayedIn = read.getString("lastPlayedIn");
            int x = read.getInt("x");
            int z = read.getInt("z");
            int size = read.getInt("size");
            int worldOffsetX = x * World.REGION_SIZE;
            int worldOffsetZ = z * World.REGION_SIZE;

            if (dataVersion > World.REGION_DATA_VERSION)
                throw new IllegalArgumentException("Unsupported region data version " + dataVersion);

            if (dataVersion < 0)
                throw new IllegalArgumentException("Invalid region data version " + dataVersion);

//            var dataVersion = stream.readInt();
//            var lastPlayedIn = stream.readUTF();
//            var x = stream.readInt();
//            var z = stream.readInt();
//            var regionSize = stream.readInt();
//            var worldOffsetX = x * World.REGION_SIZE;
//            var worldOffsetZ = z * World.REGION_SIZE;

            // Read chunks from region file.
            Map<ChunkPos, ServerChunk> chunkMap = new HashMap<>();
            var regionPos = new RegionPos(x, z);
            var region = new Region(world, regionPos, chunkMap);
            for (var key : read.keys()) {
                if (!key.matches("c\\d+;\\d+")) {
                    continue;
                }

                var parts = key.substring(1).split(";");
                var chunkX = Integer.parseInt(parts[0]);
                var chunkZ = Integer.parseInt(parts[1]);

                // Validate chunk coordinates.
                Preconditions.checkElementIndex(chunkX, World.REGION_SIZE, "Invalid chunk X position");
                Preconditions.checkElementIndex(chunkZ, World.REGION_SIZE, "Invalid chunk Z position");

                // Create local and global chunk coordinates.
                var localChunkPos = new ChunkPos(chunkX, chunkZ);

                // Load server chunk.
                MapType map = read.getMap(key);
                if (map == null) continue;
                var chunk = ServerChunk.load(world, localChunkPos, map, region);
                chunkMap.put(localChunkPos, chunk);
            }

            // Chceck if region already exists, if so, then throw an error.
            var oldRegion = this.regions.get(regionPos);
            if (oldRegion != null) {
                throw new OverwriteError("Tried to overwrite region %s".formatted(regionPos));
            }

            // Create region instance.
            this.regions.put(regionPos, region);
            this.chunkCount += region.chunkCount;
            return region;
        }

        /**
         * Gets a loaded region from the storage map based on the given chunk position.
         *
         * @param chunkPos the chunk position.
         * @return the loaded region, or null if it isn't loaded.
         */
        @Nullable
        public Region getRegionAt(ChunkPos chunkPos) {
            var regionX = chunkPos.x() / 32;
            var regionZ = chunkPos.z() / 32;

            return this.getRegion(regionX, regionZ);
        }

        /**
         * Gets a loaded region from the storage map.
         *
         * @param regionX the X-coordinate of the region.
         * @param regionZ the Z-coordinate of the region.
         * @return the loaded region, or null if it isn't loaded.
         */
        @Nullable
        private Region getRegion(int regionX, int regionZ) {
            return this.getRegion(new RegionPos(regionX, regionZ));
        }

        /**
         * Gets a loaded region from the storage map.
         *
         * @param regionPos the region position.
         * @return the loaded region, or null if it isn't loaded.
         */
        @Nullable
        private Region getRegion(RegionPos regionPos) {
            return this.regions.get(regionPos);
        }

        public void dispose() {
            this.regions.values().forEach(Region::dispose);
            this.regions.clear();
        }

        public int getChunkCount() {
            return this.chunkCount;
        }
    }

    public record RecordedChange(int x, int y, int z, BlockMetadata block) {
        public MapType save() {
            MapType mapType = new MapType();
            mapType.putInt("x", this.x);
            mapType.putInt("y", this.y);
            mapType.putInt("z", this.z);
            mapType.put("block", this.block.save());
            return mapType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RecordedChange that = (RecordedChange) o;
            return x == that.x && y == that.y && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }

        @Override
        public String toString() {
            return "RecordedChange{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    ", block=" + block +
                    '}';
        }
    }
}
