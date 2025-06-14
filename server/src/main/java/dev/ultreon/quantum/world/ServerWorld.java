package dev.ultreon.quantum.world;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.CompletionPromise;
import dev.ultreon.quantum.Promise;
import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.api.events.block.BlockBrokenEvent;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.config.QuantumServerConfig;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.debug.Debugger;
import dev.ultreon.quantum.debug.ValueTracker;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.events.EntityEvents;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.tool.ToolItem;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.s2c.*;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.ubo.types.ListType;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.data.RegionChannel;
import dev.ultreon.quantum.world.gen.FeatureData;
import dev.ultreon.quantum.world.gen.StructureData;
import dev.ultreon.quantum.world.gen.StructureInstance;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;
import dev.ultreon.quantum.world.gen.noise.NoiseConfigs;
import dev.ultreon.quantum.world.loot.LootGenerator;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.quantum.world.vec.*;
import kotlin.system.TimingKt;
import org.apache.commons.collections4.queue.SynchronizedQueue;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The ServerWorld class represents a server-side world.
 * It contains methods to manipulate and query chunks, blocks, and other
 * world data.
 */
public class ServerWorld extends World implements Audience {
    private final WorldStorage storage;
    private final ChunkGenerator generator;
    private final QuantumServer server;
    private final RegionStorage regionStorage = new RegionStorage();
    private final AsyncExecutor saveExecutor = new AsyncExecutor(1, "ServerWorld-Save");
    private final AsyncExecutor executor = new AsyncExecutor(4, "World-Executor");
    private static long chunkUnloads;

    private final Queue<ChunkVec> chunksToLoad = this.createSyncQueue();
    private final Queue<ChunkVec> chunksToUnload = this.createSyncQueue();
    private final Queue<Runnable> tasks = this.createSyncQueue();

    private int playTime;
    private final Set<RecordedChange> recordedChanges = Collections.synchronizedSet(new LinkedHashSet<>());
    private int chunksToLoadCount;
    private boolean saving;
    private long time;
    private final RegistryKey<DimensionInfo> key;
    private final FeatureData featureData = new FeatureData();
    private final StructureData structureData = new StructureData();

    private final RandomTicker randomTicker;

    public ServerWorld(QuantumServer server, RegistryKey<DimensionInfo> key, WorldStorage storage, ChunkGenerator generator, long seed, MapType worldData) {
        super(seed);

        this.key = key;

        this.server = server;
        this.storage = storage;
        this.generator = generator;

        this.load(worldData);

        this.randomTicker = new RandomTicker(this, QuantumServer.MSPT * 10);
        this.randomTicker.start();

        NoiseConfigs noiseConfigs = server.getNoiseConfigs();
        noiseConfigs.biomeX.create(this.seed);
        noiseConfigs.biomeY.create(this.seed);
        noiseConfigs.layerX.create(this.seed);
        noiseConfigs.layerY.create(this.seed);
    }

    public void recordChange(RecordedChange change) {
        this.recordedChanges.add(change);
    }

    public void recordChange(int x, int y, int z, BlockState state) {
        this.recordedChanges.add(new RecordedChange(x, y, z, state));
    }

    private void load(MapType worldData) {
        synchronized (this) {
            this.playTime = worldData.getInt("playTime", 0);
            this.uid = worldData.getUUID("uid", this.uid);
            this.spawnPoint.set(worldData.getInt("spawnX", 0), worldData.getInt("spawnY", 0), worldData.getInt("spawnZ", 0));
        }
    }

    private MapType save(MapType worldData) {
        synchronized (this) {
            worldData.putInt("playTime", this.playTime);
            worldData.putUUID("uid", this.uid);
            worldData.putInt("spawnX", spawnPoint.x);
            worldData.putInt("spawnY", spawnPoint.y);
            worldData.putInt("spawnZ", spawnPoint.z);
            worldData.putLong("seed", this.seed);

            ListType<MapType> recordedChanges = new ListType<>();
            for (RecordedChange change : this.recordedChanges) {
                recordedChanges.add(change.save());
            }

            worldData.put("RecordedChanges", recordedChanges);
        }

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
        return SynchronizedQueue.synchronizedQueue(new ArrayDeque<>());
    }

    @Override
    public int getRenderDistance() {
        return this.server.getRenderDistance();
    }

    @Override
    public boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkVec pos) {
        this.checkThread();

        this.unloadChunk(pos, true);

//        WorldEvents.CHUNK_UNLOADED.factory().onChunkUnloaded(this, chunk.getVec(), chunk);
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
     * @deprecated due to block setting changes, use {@link #set(int, int, int, BlockState, int)} instead
     */
    @Override
    @Deprecated
    public boolean set(int x, int y, int z, @NotNull BlockState block) {
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
    public boolean set(int x, int y, int z, @NotNull BlockState block,
                       @MagicConstant(flagsFromClass = BlockFlags.class) int flags) {
        boolean isBlockSet = super.set(x, y, z, block, flags);
        BlockVec blockVec = new BlockVec(x, y, z);
        block.onPlace(this, blockVec);
        if (~(flags & BlockFlags.SYNC) != 0) this.sync(x, y, z, block);
        if (~(flags & BlockFlags.UPDATE) != 0) {
            for (Direction direction : Direction.values()) {
                BlockVec offset = blockVec.offset(direction);
                BlockState blockState = this.get(offset);
                blockState.update(this, offset);
            }
        }

        return isBlockSet;
    }

    /**
     * Sets the block at the given position.
     *
     * @param pos   the coordinates of the block in world space
     * @param block the block type to set
     * @param flags the flags to use when setting the block. Values are defined in {@link BlockFlags}
     * @return true if the block was successfully set, false if setting the block failed
     * @see BlockFlags
     */
    @Override
    public boolean set(@NotNull BlockVec pos, @NotNull BlockState block,
                       @MagicConstant(flagsFromClass = BlockFlags.class) int flags) {

        int x = pos.getIntX();
        int y = pos.getIntY();
        int z = pos.getIntZ();
        boolean isBlockSet = super.set(pos, block, flags);
        BlockVec blockVec = new BlockVec(x, y, z);
        block.onPlace(this, blockVec);
        if (~(flags & BlockFlags.SYNC) != 0) this.sync(x, y, z, block);
        if (~(flags & BlockFlags.UPDATE) != 0) {
            for (Direction direction : Direction.values()) {
                BlockVec offset = blockVec.offset(direction);
                BlockState blockState = this.get(offset);
                blockState.update(this, offset);
            }
        }

        return isBlockSet;
    }

    @Override
    public void setBlockEntity(@NotNull BlockVec pos, @NotNull BlockEntity blockEntity) {
        super.setBlockEntity(pos, blockEntity);

        if (this.getBlockEntity(pos) == blockEntity) {
            this.sendAllTracking(pos.getIntX(), pos.getIntY(), pos.getIntZ(), new S2CBlockEntitySetPacket(pos, blockEntity.getType().getRawId()));
        }
    }

    @Override
    public void spawnParticles(@NotNull ParticleType particleType, @NotNull Vec3d position, @NotNull Vec3d motion, int count) {
        super.spawnParticles(particleType, position, motion, count);

        this.sendAllTracking((int) position.x, (int) position.y, (int) position.z, new S2CSpawnParticlesPacket(particleType, position, motion, count));
    }

    @Override
    public boolean destroyBlock(@NotNull BlockVec breaking, @Nullable Player breaker) {
        BlockState blockState = get(breaking);
        if (blockState.isAir()) {
            QuantumServer.LOGGER.warn("Tried to break air block at {}!", breaking);
            return false;
        }

        boolean broken = super.destroyBlock(breaking, breaker);

        ItemStack stack = breaker != null ? breaker.getSelectedItem() : ItemStack.empty();
        ModApi.getGlobalEventHandler().call(new BlockBrokenEvent(this, breaking, blockState, Blocks.AIR.getDefaultState(), stack, breaker));

        return handleBroken(breaking, breaker, broken, blockState, stack);
    }

    private boolean handleBroken(@NotNull BlockVec breaking, @Nullable Player breaker, boolean broken, BlockState blockState, ItemStack stack) {
        if (!broken) return broken;

        if (blockState.isToolRequired()
            && (!(stack.getItem() instanceof ToolItem)
                || ((ToolItem) stack.getItem()).getToolType() != blockState.getEffectiveTool()))
            return false;

        LootGenerator lootGen = blockState.getLootGen();
        if (lootGen == null) return true;
        for (ItemStack item : lootGen.generate(breaker != null ? breaker.getRng() : new JavaRNG())) {
            drop(item, breaking.vec().d().add(0.5));

            if (breaker != null) {
                breaker.sendMessage("[yellow][*][DEBUG] [white]You looted " + item.getItem().getTranslation().getText() + " from " + blockState.getBlock().getTranslation().getText() + "!");
            }
        }
        return broken;
    }

    @Override
    public boolean isLoaded(@NotNull Chunk chunk) {
        Region regionAt = regionStorage.getRegionAt(chunk.vec);
        if (regionAt == null) return false;
        return regionAt.isLoaded(chunk);
    }

    private void sync(int x, int y, int z, BlockState block) {
        this.sendAllTracking(x, y, z, new S2CBlockSetPacket(new BlockVec(x, y, z), block));
    }

    public void sendAllTracking(int x, int y, int z, Packet<? extends ClientPacketHandler> packet) {
        for (var player : this.server.getPlayers()) {
            if (player.getWorld() != this) continue;

            if (player.isChunkActive(new BlockVec(x, y, z).chunk())) {
                player.connection.send(packet);
            }
        }
    }

    public void sendAllTrackingExcept(int x, int y, int z, Packet<? extends ClientPacketHandler> packet, @NotNull ServerPlayer except) {
        for (var player : this.server.getPlayers()) {
            if (player == except) continue;

            if (player.getWorld() != this) continue;

            if (player.isChunkActive(new BlockVec(x, y, z).chunk())) {
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
    @NonBlocking
    @SuppressWarnings("UnusedReturnValue")
    public @NotNull Promise<@Nullable ServerChunk> loadChunk(ChunkVec pos) {
        // Ensure this method is called from the correct thread
        this.checkThread();

        // Load the chunk
        return this.loadChunk(pos.getIntX(), pos.getIntY(), pos.getIntZ());
    }

    /**
     * Loads a chunk at the specified global chunk position (x, z).
     *
     * @param x The x-coordinate of the chunk.
     * @param z The z-coordinate of the chunk.
     * @return The loaded chunk or null if loading failed.
     */
    @NonBlocking
    public @NotNull Promise<@Nullable ServerChunk> loadChunk(int x, int y, int z) {
        // Ensure this method is called from the correct thread
        this.checkThread();

        // Load the chunk
        return this.loadChunk(x, y, z, false);
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
    @NonBlocking
    public Promise<ServerChunk> loadChunk(int x, int y, int z, boolean overwrite) {
        // Ensure this method is called from the correct thread
        this.checkThread();

        // Calculate global and local chunk positions
        var globalVec = new ChunkVec(x, y, z, ChunkVecSpace.WORLD);
        var localVec = World.toLocalChunkVec(x, y, z);

        try {
            // Check if there's an existing chunk at the global position
            var oldChunk = this.getChunk(globalVec);
            if (!overwrite) {
                Region regionAt = this.regionStorage.getRegionAt(globalVec);
                if (regionAt == null) {
                    throw new IllegalChunkStateException("Region is not loaded.");
                }

                if (!oldChunk.active) {
                    return CompletionPromise.completedPromise((ServerChunk) regionAt.activate(localVec));
                }

                return CompletionPromise.completedPromise(oldChunk);
            }

            // Get or open the region at the global position
            var region = this.getOrOpenRegionAt(globalVec);
            var chunk = region.openChunk(globalVec);

            return chunk.thenApply(serverChunk -> {
                if (serverChunk.active) {
                    throw new IllegalChunkStateException("Chunk is already active.");
                }

                if (region.getChunk(localVec) != serverChunk)
                    throw new IllegalChunkStateException("Chunk is not loaded.");

                ServerChunk chunk1 = this.getChunk(globalVec);
                if (chunk1 != serverChunk)
                    throw new IllegalChunkStateException("Chunk is loaded at a different location: " + serverChunk.vec + " expected " + globalVec);

                // Trigger chunk loaded event and track chunk loads
//                WorldEvents.CHUNK_LOADED.factory().onChunkLoaded(this, globalVec, serverChunk);
                ValueTracker.setChunkLoads(ValueTracker.getChunkLoads() + 1);

                return serverChunk;
            }).exceptionally(throwable -> {
                if (throwable.getCause() instanceof CancellationException) {
                    CommonConstants.LOGGER.warn("Chunk load cancelled: {}", globalVec, throwable.getCause());
                    return null;
                }
                return null;
            });
        } catch (Exception e) {
            // Log and rethrow any exception that occurred during chunk loading
            World.LOGGER.error("Failed to load chunk " + globalVec + ":", e);
            throw e;
        }
    }

    /**
     * Loads a chunk at the specified global chunk position (x, z).
     *
     * @param pos The chunk position.
     * @return The loaded chunk or null if loading failed.
     */
    @Blocking
    private @NotNull Chunk loadChunkNow(ChunkVec pos) {
        // Ensure the method is called on the correct thread
        this.checkThread();

        // Load the chunk
        return this.loadChunkNow(pos.getIntX(), pos.getIntY(), pos.getIntZ());
    }

    /**
     * Loads a chunk synchronously at the specified global chunk position (x, z), optionally overwriting an existing chunk.
     *
     * @param x The x-coordinate of the chunk.
     * @param z The z-coordinate of the chunk.
     * @return The loaded chunk or null if failed to load.
     */
    @Blocking
    private ServerChunk loadChunkNow(int x, int y, int z) {
        // Ensure the method is called on the correct thread
        this.checkThread();

        // Calculate global and local positions
        var globalVec = new ChunkVec(x, y, z, ChunkVecSpace.WORLD);
        var localVec = globalVec.regionLocal();

        try {
            // Get or open the region at the global position
            var region = this.getOrOpenRegionAt(globalVec);

            // Open the chunk at the local position within the region
            var chunk = region.openChunkNow(localVec, globalVec);
            if (chunk.active) {
                throw new IllegalChunkStateException("Chunk is already active.");
            }

            // Trigger CHUNK_LOADED event and update chunk load count
//            WorldEvents.CHUNK_LOADED.factory().onChunkLoaded(this, globalVec, chunk);
            ValueTracker.setChunkLoads(ValueTracker.getChunkLoads() + 1);

            return chunk;
        } catch (Exception e) {
            // Log and rethrow any exceptions that occur during chunk loading
            World.LOGGER.error("Failed to load chunk " + globalVec + ":", e);
            throw e;
        }
    }

    @Override
    @SuppressWarnings("GDXJavaUnsafeIterator")
    public void tick() {
        this.playTime++;
        this.time++;

        Entity[] array = this.entitiesById.values().toArray().toArray(Entity.class);
        for (Entity entity1 : array) {
            if (entity1.isMarkedForRemoval()) {
                this.entitiesById.remove(entity1.getId());
                BlockVec blockVec = entity1.getBlockVec();
                this.sendAllTracking(blockVec.getIntX(), blockVec.getIntY(), blockVec.getIntZ(), new S2CRemoveEntityPacket(entity1.getId()));
                EntityEvents.REMOVED.factory().onEntityRemoved(entity1);

                entity1.onRemoved();
            }
        }

        for (var entity : array) {
            entity.tick();
        }

        var poll = this.tasks.poll();
        if (poll != null) poll.run();

        this.regionStorage.tick();

        if (this.time % 20 == 0) {
            for (ServerPlayer player : this.server.getPlayers()) {
                if (player.connection.isLoggingIn()) continue;
                player.sendPacket(new S2CTimeSyncPacket(time));
            }
        }

        this.pollChunkQueues();
    }

    private void pollChunkQueues() {
        var unload = this.chunksToUnload.poll();
        if (unload != null) {
            Debugger.log("Unloading chunk " + unload);
            var region = this.regionStorage.getRegionAt(unload);
            if (region != null && region.getActiveChunk(unload.regionLocal()) != null) {
                this.unloadChunk(unload);
            }
        }

        var load = this.chunksToLoad.poll();
        if (load != null) {
            this.loadChunk(load);
        }
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

        if (chunksToLoadCount <= 0) {
            chunksToLoadCount = refresher.toLoad.size();
        }

        for (ChunkVec pos : refresher.toLoad) {
            this.deferLoadChunk(pos);
        }

        for (ChunkVec pos : refresher.toUnload) {
            this.deferUnloadChunk(pos);
        }
    }

    private void deferLoadChunk(ChunkVec chunkVec) {
        try {
            if (this.chunksToLoad.contains(chunkVec)) return;
            this.chunksToLoad.add(chunkVec);
        } catch (Throwable t) {
            World.LOGGER.error("Failed to defer chunk " + chunkVec + ":", t);
        }
    }

    private void deferUnloadChunk(ChunkVec chunkVec) {
        try {
            if (this.chunksToUnload.contains(chunkVec)) return;
            this.chunksToUnload.add(chunkVec);
        } catch (Throwable t) {
            World.LOGGER.error("Failed to defer chunk " + chunkVec + ":", t);
        }
    }

    /**
     * Unloads a chunk from the world.
     *
     * @param chunkVec the position of the chunk to unload
     * @param save     if true, the chunk will be saved to disk.
     */
    public void unloadChunk(ChunkVec chunkVec, boolean save) {
        this.checkThread();

        if (shouldStayLoaded(chunkVec)) return;

        var region = this.regionStorage.getRegionAt(chunkVec);
        if (region == null) {
            if (DebugFlags.CHUNK_LOADER_DEBUG.isEnabled()) {
                LOGGER.debug("Region is unloaded while unloading chunk " + chunkVec);
            }

            return;
        }

        ChunkVec localChunkVec = chunkVec.regionLocal();
        if (region.getActiveChunk(localChunkVec) == null && DebugFlags.CHUNK_LOADER_DEBUG.isEnabled()) {
            LOGGER.debug("Tried to unload chunk {} but it isn't active", chunkVec);
            return;
        }

        var chunk = region.deactivate(localChunkVec);
        if (chunk == null) {
            return;
        }

        if (region.isEmpty() && save) {
            try {
                this.saveRegion(region);
            } catch (Exception e) {
                World.LOGGER.warn("Failed to save region {}:", region.getPos(), e);
                return;
            }
        }

        this.server.onChunkUnloaded(chunk);
        chunk.dispose();

        ServerWorld.chunkUnloads++;
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
        if (!QuantumServer.isOnServerThread()) throw new InvalidThreadException("Should be on server thread.");
    }

    @Override
    public @Nullable ServerChunk getChunkAt(int x, int y, int z) {
        BlockVec blockVec = new BlockVec(x, y, z);

        if (this.isOutOfWorldBounds(x, y, z)) return null;

        ChunkVec chunkVec = blockVec.chunk();
        return this.getChunk(chunkVec);
    }

    public @Nullable ServerChunk getChunkAtNoLoad(BlockVec pos) {
        if (this.isOutOfWorldBounds(pos)) return null;

        ChunkVec chunkVec = pos.chunk();
        return this.getChunkNoLoad(chunkVec);
    }

    public @Nullable ServerChunk getChunkNoLoad(ChunkVec pos) {
        // Get the region at the specified position
        var region = this.getOrOpenRegionAt(pos);

        // Get the chunk from the region
        var chunk = region.getChunk(pos.regionLocal());

        // If the chunk is found, verify its position matches the expected position
        if (chunk != null && DebugFlags.CHUNK_LOADER_DEBUG.isEnabled()) {
            ChunkVec foundAt = chunk.vec;

            // If the positions don't match, throw a validation error
            if (!foundAt.equals(pos))
                throw new ValidationError(String.format("Chunk expected to be found at %s was found at %s instead.", pos, foundAt));
        }

        if (DebugFlags.CHUNK_LOADER_DEBUG.isEnabled() && chunk == null)
            LOGGER.debug("Chunk not found at {}", pos);

        // Return the chunk
        return chunk;
    }

    /**
     * Retrieves the chunk at the given position.
     *
     * @param globalVec The position of the chunk.
     * @return The chunk at the given position, or null if not found.
     */
    @Override
    public @NotNull ServerChunk getChunk(@NotNull ChunkVec globalVec) {
        // Get the region at the specified position
        var region = this.getOrOpenRegionAt(globalVec);

        // Get the chunk from the region
        var chunk = region.getChunk(globalVec.regionLocal());

        // If the chunk is found, verify its position matches the expected position
        if (chunk != null && DebugFlags.CHUNK_LOADER_DEBUG.isEnabled()) {
            ChunkVec foundAt = chunk.vec;

            // If the positions don't match, throw a validation error
            if (!foundAt.equals(globalVec))
                throw new ValidationError(String.format("Chunk expected to be found at %s was found at %s instead.", globalVec, foundAt));
        }

        if (DebugFlags.CHUNK_LOADER_DEBUG.isEnabled() && chunk == null)
            LOGGER.debug("Chunk not found at {}", globalVec);

        if (chunk == null) {
            return (ServerChunk) loadChunkNow(globalVec);
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
        return regions.stream().flatMap(value -> value.getChunks().stream()).collect(Collectors.toList());
    }

    /**
     * Disposes the world, and cleans up the objects it uses.
     * Added for clean closing of the world.
     */
    @Override
    @ApiStatus.Internal
    public void dispose() {
        this.disposed = true;
//        var saveSchedule = this.saveSchedule;
//        if (saveSchedule != null) saveSchedule.cancel(true);
        this.saveExecutor.dispose();

        super.dispose();

        this.generator.dispose();
        this.randomTicker.dispose();
        this.regionStorage.dispose();
    }

    @Override
    public int getTotalChunks() {
        return this.regionStorage.getChunkCount();
    }

    @Override
    public BreakResult continueBreaking(@NotNull BlockVec breaking, float amount, @NotNull Player breaker) {
        return super.continueBreaking(breaking, amount, breaker);
    }

    @Override
    public void setSpawnPoint(int spawnX, int spawnZ) {
        this.getChunkAt(spawnX, 0, spawnZ);
        this.spawnPoint.set(spawnX, this.getHeight(spawnX, spawnZ) + 1, spawnZ);
    }

    @NotNull
    @Override
    @Blocking
    public BlockVec getSpawnPoint() {
        int height = getHeight(spawnX, spawnZ, HeightmapType.MOTION_BLOCKING) + 1;
        if (height != 256) {
            return new BlockVec(spawnX, height, spawnZ);
        }

        return new BlockVec(spawnX, 256, spawnZ);
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
        this.generator.create(this, seed);

        this.storage.createDir("players/");
        this.storage.createDir("regions/");

        FileHandle dimPath = this.getDimensionPath();

        World.LOGGER.info("Loading world: " + dimPath.name());

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
        // TODO
//        this.saveSchedule = this.server.schedule(new Task<>(new NamespaceID("auto_save")) {
//            @Override
//            public void run() {
//                try {
//                    ServerWorld.this.save(true);
//                } catch (Exception e) {
//                    World.LOGGER.error("Failed to save world:", e);
//                }
//                ServerWorld.this.saveSchedule = ServerWorld.this.server.schedule(this, QuantumServerConfig.autoSaveInterval, TimeUnit.SECONDS);
//            }
//        }, QuantumServerConfig.initialAutoSaveDelay, TimeUnit.SECONDS);
        //</editor-fold>

//        WorldEvents.LOAD_WORLD.factory().onLoadWorld(this, this.storage);

        World.LOGGER.info("Loaded world: " + dimPath.name());
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

        FileHandle dimPath = getDimensionPath();

        // Log saving world message if not silent
        if (!silent) World.LOGGER.info("Saving world: " + dimPath.name());

        // Save entities data
        var entitiesData = new ListType<MapType>();

        //noinspection GDXJavaUnsafeIterator
        Array<Entity> entityArray = this.entitiesById.values().toArray();
        for (var entity : entityArray.toArray(Entity.class)) {
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
                World.LOGGER.warn("Failed to save region {}:", e, region.getPos());
                var remove = this.regionStorage.regions.remove(region.getPos());
                if (remove != region)
                    this.server.crash(new ValidationError("Removed region is not the region that got saved."));
                region.dispose();
            }
        }

        // Trigger save world event
//        WorldEvents.SAVE_WORLD.factory().onSaveWorld(this, this.storage);

        // Log saved world message if not silent
        if (!silent) World.LOGGER.info("Saved world: " + dimPath.name());
        this.saving = false;
    }

    private FileHandle getDimensionPath() {
        return this.storage.getDirectory().child("regions/" + key.id().getDomain() + "/" + key.id().getPath() + ".region");
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
        try {
            this.regionStorage.save(region, file, dispose);
            if (!region.dirtyWhileSaving) region.dirty = false;
            else region.dirtyWhileSaving = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
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
    public Promise<Boolean> saveAsync(boolean silent) {
        // Check if there is a save schedule running
//        var saveSchedule = this.saveSchedule;

        // If there is a save schedule running and it's not done, return the existing saveFuture if available
//        if (saveSchedule != null && !saveSchedule.isDone()) {
//            return this.saveFuture != null ? this.saveFuture : CompletionPromise.completedFuture(true);
//        }

        // If there is a save schedule running, cancel it
//        if (saveSchedule != null) {
//            saveSchedule.cancel(false);
//        }

        try {
            // Run the save operation asynchronously
            return Promise.supplyAsync(() -> {
                try {
                    //noinspection BlockingMethodInNonBlockingContext
                    this.save(silent);
                    return true;
                } catch (Exception e) {
                    // Handle save error
                    this.server.handleWorldSaveError(e);
                    World.LOGGER.error("Failed to save world", e);
                    return false;
                }
            });
        } catch (Exception e) {
            // Log error if save operation fails
            World.LOGGER.error("Failed to save world", e);
            return CompletionPromise.completedPromise(false);
        }
    }

    /**
     * Gets or opens the region at the specified chunk position.
     *
     * @param chunkVec the chunk position to get or open the region at
     * @return the region at the specified chunk position
     */
    private @NotNull Region getOrOpenRegionAt(ChunkVec chunkVec) {
        var regionVec = chunkVec.region();

        // Get the map of regions
        var regions = this.regionStorage.regions;

        // Check if the region already exists at the calculated position
        var oldRegion = regions.get(regionVec);
        if (oldRegion != null) {
            return oldRegion;
        }

        // If the region does not exist, try to open it
        try {
            if (this.storage.regionExists(regionVec.x, regionVec.y, regionVec.z)) {
                return this.openRegion(regionVec.x, regionVec.y, regionVec.z);
            }
        } catch (Exception e) {
            // Log error if the region failed to load
            World.LOGGER.error("Region at {} run failed to load:", regionVec, e);
        }

        // Create a new region if it doesn't exist and add it to the regions map
        Region region;
        try {
            region = new Region(this, regionVec, new RegionChannel(this.storage.regionFile(regionVec.x, regionVec.y, regionVec.z).file()));
        } catch (IOException e) {
            throw new GdxRuntimeException("Failed to create region: " + regionVec, e);
        }
        region.initialize();
        this.regionStorage.regions.put(regionVec, region);
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
    private @NotNull Region openRegion(int rx, int ry, int rz) throws IOException {
        var fileHandle = this.storage.regionFile(rx, ry, rz);
        return this.regionStorage.load(this, new RegionVec(rx, ry, rz), new RegionChannel(fileHandle.file()));
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

        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) entity;
            sendAllTracking(spawn.getBlockVec().getIntX(), spawn.getBlockVec().getIntY(), spawn.getBlockVec().getIntZ(), new S2CAddPlayerPacket(player.getId(), player.getUuid(), player.getName(), new Vec3d(spawn.getBlockVec().getIntX() + 0.5, spawn.getBlockVec().getIntY(), spawn.getBlockVec().getIntZ() + 0.5)));
        } else
            sendAllTracking(spawn.getBlockVec().getIntX(), spawn.getBlockVec().getIntY(), spawn.getBlockVec().getIntZ(), new S2CAddEntityPacket(spawn));

        return spawn;
    }

    @Override
    public <T extends Entity> T spawn(@NotNull T entity, @NotNull MapType spawnData) {
        if (!(entity instanceof ServerPlayer) && entity instanceof Player)
            throw new IllegalStateException("Tried to spawn a non-server player in a server world.");

        T spawn = super.spawn(entity, spawnData);

        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) entity;
            sendAllTracking(spawn.getBlockVec().getIntX(), spawn.getBlockVec().getIntY(), spawn.getBlockVec().getIntZ(), new S2CAddPlayerPacket(player.getId(), player.getUuid(), player.getName(), new Vec3d(spawn.getBlockVec().getIntX() + 0.5, spawn.getBlockVec().getIntY(), spawn.getBlockVec().getIntZ() + 0.5)));
        } else
            sendAllTracking(spawn.getBlockVec().getIntX(), spawn.getBlockVec().getIntY(), spawn.getBlockVec().getIntZ(), new S2CAddEntityPacket(spawn));

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
        if (!QuantumServer.isOnServerThread()) {
            QuantumServer.invokeAndWait(() -> this.prepareSpawn(player));
            return;
        }

        ChunkVec origin = player.getChunkVec();
        for (int x = origin.getIntX() - 1; x <= origin.getIntX() + 1; x++) {
            for (int y = origin.getIntY() - 1; y <= origin.getIntY() + 1; y++) {
                for (int z = origin.getIntZ() - 1; z <= origin.getIntZ() + 1; z++) {
                    this.loadChunk(x, y, z);
                }
            }
        }
    }

    /**
     * @return the server this world belongs to.
     */
    public QuantumServer getServer() {
        return this.server;
    }

    /**
     * Should only be used for debugging.<br>
     * Note that this method is not thread safe.<br>
     * Only run this method in the server thread.<br>
     * Be sure to remove the client chunk before calling this.
     *
     * @param globalVec the position of the chunk in global space.
     */
    @ApiStatus.Experimental
    public void regenerateChunk(ChunkVec globalVec) {
        this.checkThread();
        this.unloadChunk(globalVec);
        Region region = this.getOrOpenRegionAt(globalVec);
        var localVec = World.toLocalChunkVec(globalVec);
        region.chunks.remove(localVec);
        region.chunkCount--;
        regionStorage.chunkCount--;
        region.generateChunk(globalVec);
    }

    /**
     * Sets the spawn point for the world.<br>
     * The spawn point is set randomly.
     */
    public void setupSpawn() {
        int spawnChunkX = MathUtils.random(-CS * 2, 31);
        int spawnChunkZ = MathUtils.random(-CS * 2, 31);
        int spawnX = MathUtils.random(spawnChunkX * CS, spawnChunkX * CS + 15);
        int spawnZ = MathUtils.random(spawnChunkZ * CS, spawnChunkZ * CS + 15);

        QuantumServer.invokeAndWait(() -> this.setSpawnPoint(spawnX, spawnZ));
    }

    public void recordOutOfBounds(int x, int y, int z, BlockState block) {
//        if (this.isOutOfWorldBounds(x, y, z)) {
//            return;
//        }
//
//        Chunk chunkAt = this.getChunkAt(x, y, z);
//        if (chunkAt == null) {
//            if (WorldGenDebugContext.isActive())
//                System.out.println("[DEBUG] Recorded out of bounds block at " + x + " " + y + " " + z + " " + block);
//            this.recordedChanges.add(new RecordedChange(x, y, z, block));
//            return;
//        }
//
//        if (WorldGenDebugContext.isActive())
//            System.out.println("[DEBUG] Chunk is available, setting block at " + x + " " + y + " " + z + " " + block);
//
//        chunkAt.setFast(World.toLocalBlockVec(x, y, z).vec(), block);

        // NO
    }

    public ChunkGenerator getGenerator() {
        return this.generator;
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
    public void sync(BlockVec pos) {
        this.sync(pos.getIntX(), pos.getIntY(), pos.getIntZ(), this.get(pos));
    }

    /**
     * Check if the chunk at the given position is loaded.
     *
     * @param blockVec the position of the block
     * @return true if the chunk is loaded
     */
    public boolean isLoaded(BlockVec blockVec) {
        return this.getChunkAtNoLoad(blockVec) != null;
    }

    /**
     * Check if the chunk at the given position is loaded.
     *
     * @param chunkVec the position of the chunk
     * @return true if the chunk is loaded
     */
    public boolean isLoaded(ChunkVec chunkVec) {
        return this.getChunkNoLoad(chunkVec) != null;
    }

    public @NotNull Promise<@Nullable ServerChunk> getOrLoadChunk(ChunkVec pos) {
        @Nullable ServerChunk chunk = this.getChunkNoLoad(pos);
        if (chunk == null) {
            return this.loadChunk(pos);
        }

        return CompletionPromise.completedPromise(chunk);
    }

    public void stopTrackingChunk(ChunkVec vec, ServerPlayer serverPlayer) {
        ServerChunk chunk = this.getChunkNoLoad(vec);
        if (chunk != null) {
            chunk.stopTracking(serverPlayer);
        }
    }

    public Stream<Vec3d> getCavePointsFor(@NotNull ChunkVec vec) {
        return getOrOpenRegionAt(vec).caveCache.stream().filter(vec3d -> {
            BlockVec start = vec.start().regionLocal();
            return vec3d.x >= start.x && vec3d.y >= start.y && vec3d.z >= start.z &&
                   vec3d.x < start.x + CS && vec3d.y < start.y + CS && vec3d.z < start.z + CS;
        });
    }

    public String executorStatus() {
        return executor.toString();
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSaveable() {
        return true;
    }

    @Override
    public RegistryKey<DimensionInfo> getDimension() {
        return key;
    }

    public Collection<StructureInstance> getStructuresAt(ChunkVec vec) {
        return structureData.getStructuresAt(vec);
    }

    public FeatureData getFeatureData() {
        return featureData;
    }

    @Override
    public void sendPacket(Packet<? extends ClientPacketHandler> packet) {
        for (ServerPlayer player : getServer().getPlayers()) {
            if (player.getWorld() == this) {
                player.sendPacket(packet);
            }
        }
    }

    @Override
    public void sendMessage(String message) {
        for (ServerPlayer player : getServer().getPlayers()) {
            if (player.getWorld() == this) {
                player.sendMessage(message);
            }
        }
    }

    @Override
    public void sendMessage(TextObject message) {
        for (ServerPlayer player : getServer().getPlayers()) {
            if (player.getWorld() == this) {
                player.sendMessage(message);
            }
        }
    }

    /**
     * The region class.
     * Note: This class is not thread safe.
     *
     * @author <a href="https://github.com/XyperCode">Qubilux</a>
     * @since 0.1.0
     */
    @NotThreadSafe
    public static class Region implements Disposable {
        private final RegionVec pos;
        public int dataVersion;
        public String lastPlayedIn = QuantumServer.get().getGameVersion();
        public boolean saving;
        public boolean dirtyWhileSaving;
        public RegionChannel channel;
        private Map<ChunkVec, ServerChunk> chunks = new ConcurrentHashMap<>();
        private boolean disposed = false;
        private final ServerWorld world;
        private final List<ChunkVec> generatingChunks = new CopyOnWriteArrayList<>();
        private final Object buildLock = new Object();
        private boolean dirty;
        private int chunkCount;
        private final Set<Vec3d> caveCache = new HashSet<>();

        /**
         * Constructs a new region with the given world and position.
         *
         * @param world the world this region belongs to.
         * @param pos   the position of the region.
         */
        public Region(ServerWorld world, RegionVec pos, RegionChannel channel) {
            this.world = world;
            this.pos = pos;
            this.channel = channel;
        }

        /**
         * Constructs a new region with the given world and position. It also preloads all chunks.
         *
         * @param world   the world this region belongs to.
         * @param pos     the position of the region.
         * @param chunks  the chunks to load into the region.
         * @param channel the region channel for saving/loading worlds dynamically.
         */
        public Region(ServerWorld world, RegionVec pos, Map<ChunkVec, ServerChunk> chunks, RegionChannel channel) {
            this.world = world;
            this.pos = pos;
            this.chunks = chunks;
            this.channel = channel;
        }

        /**
         * @return all loaded chunks within the region.
         */
        public Collection<ServerChunk> getChunks() {
            return this.chunks.values().stream().filter(chunk -> chunks.containsKey(chunk.vec)).collect(Collectors.toList());
        }

        /**
         * @return the position of the region.
         */
        public RegionVec pos() {
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
            synchronized (this) {
                if (this.disposed) return;
                this.disposed = true;

                for (Chunk value : this.chunks.values()) {
                    value.dispose();
                }

                this.chunks.clear();
                try {
                    this.channel.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * Deactivates the chunk at the given position.
         * The chunk will still be loaded in memory for reuse.
         *
         * @param chunkVec the local position of the chunk to deactivate.
         * @return the deactivated chunk, or null if the chunk wasn't loaded.
         */
        @ApiStatus.Internal
        public @Nullable ServerChunk deactivate(@NotNull ChunkVec chunkVec) {
            synchronized (this) {
                ServerChunk chunk = this.chunks.remove(chunkVec);
                if (chunk == null)
                    return null; // Already deactivated

                if (chunk.original || !chunk.modified) return chunk;
                QuantumServer.invoke(() -> {
                    ChunkVec vec = chunk.vec.regionLocal();
                    try {
                        this.channel.saveChunk(vec.x, vec.y, vec.z, chunk.save());
                        this.channel.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                return chunk;
            }
        }

        /**
         * Activate the chunk at the given position.
         *
         * @param localVec the local position of the chunk to activate.
         * @return the acivated chunk, or null if the chunk wasn't loaded.
         */
        public @Nullable Chunk activate(@NotNull ChunkVec localVec) {
            synchronized (this) {
                @Nullable ServerChunk chunk = this.chunks.get(localVec);

                if (chunk == null) return null;

                if (this.chunks.containsKey(localVec)) return chunk;

                ChunkVec vec = chunk.vec.regionLocal();
                try {
                    chunk = ServerChunk.load(world, this.pos.chunkInWorld(localVec), this.channel.loadChunk(vec.x, vec.y, vec.z), this);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                this.chunks.put(localVec, chunk);
                chunk.active = true;

                return chunk;
            }
        }

        /**
         * Get a currently loaded chunk with the specified local position.
         *
         * @param globalVec the position of the chunk to get.
         * @return the chunk at that position, or null if the chunk wasn't loaded.
         */
        public @Nullable ServerChunk getChunk(ChunkVec globalVec) {
            synchronized (this) {
                ServerChunk serverChunk = this.chunks.get(globalVec.regionLocal());
                if (serverChunk != null) return serverChunk;
                ChunkVec localVec = globalVec.regionLocal();
                try {
                    MapType chunkData = this.channel.loadChunk(localVec.x, localVec.y, localVec.z);
                    if (chunkData == null) {
                        return null;
                    }

                    serverChunk = ServerChunk.load(world, this.pos.chunkInWorld(localVec), chunkData, this);
                } catch (IOException e) {
                    CommonConstants.LOGGER.warn("Failed to load chunk " + globalVec, e);
                    world.server.handleChunkLoadFailure(globalVec, "IO Error: " + e.getMessage());
                    return null;
                } catch (Exception e) {
                    CommonConstants.LOGGER.warn("Failed to load chunk " + globalVec, e);
                    world.server.handleChunkLoadFailure(globalVec, e.toString());
                    return null;
                }
                return serverChunk;
            }
        }

        /**
         * Get the currently active chunks in this section.
         *
         * @return the active chunks.
         */
        public Set<ChunkVec> getActiveChunks() {
            return Collections.unmodifiableSet(this.chunks.keySet());
        }

        /**
         * Get the active chunk at the specified position;
         *
         * @param localVec the position of the active chunk to get.
         * @return the active chunk, or null if there's no active chunk at the specified position.
         */
        public @Nullable Chunk getActiveChunk(ChunkVec localVec) {
            synchronized (this) {
                if (!this.chunks.containsKey(localVec)) {
                    return null;
                }
                return this.chunks.get(localVec);
            }
        }

        /**
         * @return true if this region has active chunks.
         */
        public boolean hasActiveChunks() {
            synchronized (this) {
                return !this.chunks.isEmpty();
            }
        }

        /**
         * @return true if there are no active chunks.
         */
        public boolean isEmpty() {
            synchronized (this) {
                return this.chunks.isEmpty();
            }
        }

        private void validateThread() {
            if (!QuantumServer.isOnServerThread()) {
                throw new InvalidThreadException("Should be on server thread.");
            }
        }

        /**
         * Generate a chunk at the specified chunk position local to the region.
         *
         * @param globalVec the global position of the chunk to generate.
         */
        public Promise<@NotNull ServerChunk> generateChunk(ChunkVec globalVec) {
            return this.buildChunkAsync(globalVec);
        }

        /**
         * Generate a chunk at the specified chunk position local to the region.
         *
         * @param globalVec the global position of the chunk to generate.
         * @return the generated chunk.
         */
        public @NotNull ServerChunk generateChunkNow(ChunkVec globalVec) {
            this.validateThread();

            // Add the chunk to the list of generating chunks.
            // Will return immediately if the chunk is already being built.
            synchronized (this.buildLock) {
                if (this.generatingChunks.contains(globalVec)) {
                    World.LOGGER.warn("Chunk might get overwritten");
                } else {
                    this.generatingChunks.add(globalVec);
                }
            }

            var ref = new Object() {
                ServerChunk builtChunk = null;
            };
            try {
                ref.builtChunk = this.buildChunk(globalVec);
            } catch (CancellationException e) {
                this.generatingChunks.remove(globalVec);
                throw new CancellationException("Chunk was cancelled during build");
            } catch (Throwable t) {
                this.generatingChunks.remove(globalVec);
                World.LOGGER.error("Failed to build chunk at {}:", globalVec, t);
                CrashLog crash = world.server.crash(t);
                throw new ApplicationCrash(crash);
            }

            if (!ref.builtChunk.vec.equals(globalVec)) {
                throw new IllegalStateException("Built chunk has wrong position: " + ref.builtChunk.vec);
            }

            this.world.server.onChunkBuilt(ref.builtChunk);

            var players = this.world.getServer().getPlayersInChunk(globalVec);
            players.forEach(player -> {
                try {
                    BlockVec globalTpPos = player.getBlockVec();
                    BlockVec localTpPos = globalTpPos.chunkLocal();
                    int x = localTpPos.getIntX();
                    int z = localTpPos.getIntZ();
                    Integer ascend = ref.builtChunk.ascend(x, (int) player.getY(), z);
                    if (ascend == null) return;
                    player.teleportTo(globalTpPos.getIntX(), ascend, globalTpPos.getIntZ());
                } catch (Exception e) {
                    World.LOGGER.error("Failed to teleport player outside unloaded chunk:", e);
                }
            });

            if (ref.builtChunk == null) {
                throw new IllegalChunkStateException("Chunk not built!");
            }
            return ref.builtChunk;
        }

        /**
         * Build the chunk at the specified chunk position local to the region.
         * Note: this method is asynchronous.
         *
         * @param globalVec the global position of the chunk to generate.
         */
        private Promise<@NotNull ServerChunk> buildChunkAsync(ChunkVec globalVec) {
            if (QuantumServerConfig.debugWarnChunkBuildOverload) {
                World.LOGGER.warn("Chunk building is being overloaded!");
            }

            // Add the chunk to the list of generating chunks.
            // Will return immediately if the chunk is already being built.
            synchronized (this.buildLock) {
                if (this.generatingChunks.contains(globalVec))
                    return CompletionPromise.failedPromise(new CancellationException("Chunk is already being built"));
                this.generatingChunks.add(globalVec);
            }

            // Build the chunk asynchronously.
            return Promise.supplyAsync(
                    () -> buildChunkInAsync(globalVec),
                    this.world.executor
            ).thenApplyAsync(
                    builtChunk -> QuantumServer.invoke(() -> handleChunkBuildError(globalVec, builtChunk)).join(),
                    this.world.executor);
        }

        private @NotNull ServerChunk buildChunkInAsync(ChunkVec globalVec) {
            try {
                var ref = new Object() {
                    ServerChunk builtChunk = null;
                };

                @SuppressWarnings("UnnecessaryLocalVariable") // It's a necessary local variable, IntelliJ is broken
                long l = TimingKt.measureTimeMillis(() -> {
                    ref.builtChunk = this.buildChunk(globalVec);
                    return null;
                });

                ref.builtChunk.info.buildDuration = l;

                return ref.builtChunk;
            } catch (CancellationException | RejectedExecutionException e) {
                throw e;
            } catch (Throwable e) {
                QuantumServer.LOGGER.error("Failed to build chunk at {}:", globalVec, e);
                throw e;
            }
        }

        private @Nullable ServerChunk handleChunkBuildError(ChunkVec globalVec, ServerChunk builtChunk) {
            try {
                var players = this.world.getServer().getPlayersInChunk(globalVec);
                players.forEach(player -> {
                    try {
                        BlockVec globalTpVec = player.getBlockVec();
                        BlockVec localTpVec = globalTpVec.chunkLocal();
                        int x = localTpVec.getIntX();
                        int z = localTpVec.getIntZ();
                        Integer ascend = builtChunk.ascend(x, (int) player.getY(), z);
                        if (ascend == null) return;
                        player.teleportTo(globalTpVec.getIntX(), ascend, globalTpVec.getIntZ());
                    } catch (Exception e) {
                        World.LOGGER.error("Failed to teleport player outside unloaded chunk:", e);
                    }
                });

                if (!globalVec.equals(builtChunk.vec)) {
                    World.LOGGER.error(String.format("Failed to build chunk at {} as it was generated at {} instead of " + globalVec, globalVec, builtChunk.vec));
                    throw new IllegalChunkStateException("Chunk generated at " + globalVec + " instead of " + builtChunk.vec);
                }
                return builtChunk;
            } catch (Exception e) {
                QuantumServer.LOGGER.error("Failed to build chunk at {}:", globalVec, e);

                return null;
            }
        }

        private @NotNull ServerChunk buildChunk(ChunkVec globalVec) {
            if (globalVec.getSpace() != ChunkVecSpace.WORLD)
                throw new IllegalArgumentException("Chunk vector must be in world space");
            var chunk = new BuilderChunk(this.world, Thread.currentThread(), globalVec, this);

            if (this.chunks.containsKey(globalVec.regionLocal()))
                throw new IllegalChunkStateException("Chunk already exists at " + globalVec);

            // Generate terrain using the terrain generator.
            List<RecordedChange> recordedChanges1;
            recordedChanges1 = List.copyOf(world.recordedChanges);
            this.world.generator.generate(world, chunk, recordedChanges1);

//            WorldEvents.CHUNK_BUILT.factory().onChunkGenerated(this.world, this, chunk);

            // Put the chunk into the list of loaded chunks.
            ServerChunk builtChunk = chunk.build();

            ServerChunk put = this.chunks.putIfAbsent(globalVec.regionLocal(), builtChunk);
            if (put != null)
                throw new IllegalChunkStateException("Chunk was overwritten at " + globalVec);
            this.world.regionStorage.chunkCount++;
            this.chunkCount++;

            // Mark the chunk as ready.
            builtChunk.ready = true;

            // Chunk isn't generating anymore.
            this.generatingChunks.remove(globalVec);

            if (!builtChunk.vec.equals(globalVec)) {
                throw new IllegalChunkStateException("Chunk generated at " + globalVec + " instead of " + builtChunk.vec);
            }

            return builtChunk;
        }

        /**
         * Opens a chunk at a specified position. If it isn't loaded yet, it will defer generating the chunk.
         *
         * @param globalVec the global position.
         * @return the loaded/generated chunk.
         */
        public Promise<@NotNull ServerChunk> openChunk(ChunkVec globalVec) {
            this.validateThread();

            @Nullable ServerChunk loadedChunk = this.getChunk(globalVec);
            if (loadedChunk == null) {
                return this.generateChunk(globalVec);
            }

            var loadedAt = loadedChunk.vec;
            if (!loadedAt.equals(globalVec)) {
                throw new IllegalChunkStateException(String.format("Chunk requested to load at %s got loaded at %s instead", globalVec, loadedAt));
            }

            return CompletionPromise.completedPromise(loadedChunk);
        }

        @ApiStatus.Internal
        public void tick() {
            List<ServerChunk> serverChunks;
            synchronized (this) {
                serverChunks = List.copyOf(this.chunks.values());
            }
            for (ServerChunk chunk : serverChunks) {
                chunk.tick();
            }
        }

        /**
         * Opens a chunk at a specified position. If it isn't loaded yet, it will defer generating the chunk.
         *
         * @param localVec  the region local position of the chunk to open.
         * @param globalVec the global position.
         * @return the loaded/generated chunk.
         */
        public @NotNull ServerChunk openChunkNow(ChunkVec localVec, ChunkVec globalVec) {
            this.validateThread();

            @Nullable ServerChunk loadedChunk = this.getChunk(globalVec);
            if (loadedChunk == null) return this.generateChunkNow(globalVec);

            this.world.server.onChunkLoaded(loadedChunk);

            var loadedAt = loadedChunk.vec;
            if (!loadedAt.equals(globalVec)) {
                throw new IllegalChunkStateException(String.format("Chunk requested to load at %s got loaded at %s instead", localVec, loadedAt));
            }

            return loadedChunk;
        }

        public RegionVec getPos() {
            return this.pos;
        }

        public boolean isDirty() {
            return dirty;
        }

        public void markDirty() {
            if (saving) this.dirtyWhileSaving = true;
            this.dirty = true;
        }

        public <T> Result<T> writeLocked(Supplier<T> supplier) {
            synchronized (this) {
                return Result.ok(supplier.get());
            }
        }

        public int getChunkCount() {
            return this.chunkCount;
        }

        public boolean isLoaded(@NotNull Chunk chunk) {
            if (chunk instanceof ServerChunk) {
                return this.chunks.containsKey(chunk.vec.regionLocal()) || this.chunks.containsValue(chunk);
            }

            return false;
        }

        public int getStartX() {
            return 0;
        }

        public int getStartY() {
            return 0;
        }

        public int getStartZ() {
            return 0;
        }

        public int getEndX() {
            return 512;
        }

        public int getEndY() {
            return 512;
        }

        public int getEndZ() {
            return 512;
        }

        public boolean isWithinBounds(int x, int y, int z) {
            return x >= 0 && y >= 0 && z >= 0 && x < 512 && y < 512 && z < 512;
        }

        public void setCave(int x, int y, int z) {
            this.caveCache.add(new Vec3d(x, y, z));
        }

        public void initialize() {
//            CaveCarver caveCarver = new CaveCarver(this);
//            caveCarver.generateCaves();
        }
    }

    /**
     * Represents a collection of regions.
     *
     * @author <a href="https://github.com/XyperCode">Qubilux</a>
     * @since 0.1.0
     */
    public static class RegionStorage {
        private final Map<RegionVec, Region> regions = new ConcurrentHashMap<>();
        private int chunkCount;

        /**
         * Saves a region to an output stream.
         *
         * @param region  the region to save.
         * @param file    the output stream to save to.
         * @param dispose if true, the region will be disposed after saving.
         * @throws IOException if an I/O error occurs.
         */
        @ApiStatus.Internal
        public void save(Region region, FileHandle file, boolean dispose) throws IOException {
            synchronized (this) {
                var pos = region.pos();

                Map<ChunkVec, ServerChunk> chunkMap = region.chunks;
                region.world.server.onSaveEvent(SaveEventType.REGION_START, chunkMap.size());

                // Write chunks to the region file.
                var chunks = chunkMap.values().stream().filter(chunk -> !(chunk.original || !chunk.modified)).collect(Collectors.toList());
                var idx = 0;
                CommonConstants.LOGGER.info("Saving {} chunks in region {}", chunks.size(), pos);
                int errorCount = 0;
                for (var chunk : chunks) {
                    region.world.server.onSaveEvent(SaveEventType.CHUNK_START);
                    try {
                        if (idx >= World.REGION_SIZE * World.REGION_SIZE * World.REGION_SIZE)
                            throw new IllegalArgumentException("Too many chunks in region!");
                        if (!chunk.modified || chunk.original) continue;
                        var vec = World.toLocalChunkVec(chunk.vec);
                        region.channel.saveChunk(vec.x, vec.y, vec.z, chunk.save());
                        chunk.modified = false;
                        idx++;
                    } catch (Exception e) {
                        CommonConstants.LOGGER.warn("Failed to save chunk " + chunk.vec, e);
                        errorCount++;
                    }
                    region.world.server.onSaveEvent(SaveEventType.CHUNK_END);
                }
                region.channel.flush();
                if (errorCount > 0) {
                    region.world.server.handleIOError(errorCount + " chunks failed to save!", "Check logs for more inforamtion");
                }

                // Dispose the region if requested.
                if (dispose) {
                    this.regions.remove(region.getPos());
                    this.chunkCount -= region.getChunkCount();
                    QuantumServer.invokeAndWait(region::dispose);
                }

                region.world.server.onSaveEvent(SaveEventType.REGION_END);
            }
        }

        public void tick() {
            this.regions.values().forEach(Region::tick);
        }

        /**
         * Loads a region from an input stream.
         *
         * @param world  the world to load the region in.
         * @return the loaded region.
         * @throws IOException if an I/O error occurs.
         */
        public Region load(ServerWorld world, RegionVec regionVec, RegionChannel channel) throws IOException {
            // Read chunks from region file.
            Map<ChunkVec, ServerChunk> chunkMap = new HashMap<>();
            var region = new Region(world, regionVec, chunkMap, channel);

            synchronized (this) {
                // Check if a region already exists, if so, then throw an error.
                var oldRegion = this.regions.get(regionVec);
                if (oldRegion != null) {
                    throw new OverwriteError(String.format("Tried to overwrite region %s", regionVec));
                }

                // Create region instance.
                this.regions.put(regionVec, region);
                this.chunkCount += region.chunkCount;
            }
            return region;
        }

        /**
         * Gets a loaded region from the storage map based on the given chunk position.
         *
         * @param globalVec the chunk position.
         * @return the loaded region, or null if it isn't loaded.
         */
        public @Nullable Region getRegionAt(ChunkVec globalVec) {
            return this.getRegion(globalVec.region());
        }

        /**
         * Gets a loaded region from the storage map.
         *
         * @param vec the region position.
         * @return the loaded region, or null if it isn't loaded.
         */
        private @Nullable Region getRegion(RegionVec vec) {
            return this.regions.get(vec);
        }

        public void dispose() {
            synchronized (this) {
                this.regions.values().forEach(Region::dispose);
                this.regions.clear();
            }
        }

        public int getChunkCount() {
            return this.chunkCount;
        }
    }

    public static final class RecordedChange {
        private final int x;
        private final int y;
        private final int z;
        private final BlockState block;

        public RecordedChange(int x, int y, int z, BlockState block) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.block = block;
        }

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

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int z() {
            return z;
        }

        public BlockState block() {
            return block;
        }


    }
}
