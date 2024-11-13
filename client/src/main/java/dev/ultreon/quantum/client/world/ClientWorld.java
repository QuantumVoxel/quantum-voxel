package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.*;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.debug.BoxGizmo;
import dev.ultreon.quantum.client.debug.Gizmo;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.player.RemotePlayer;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.util.Rot;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.network.packets.c2s.C2SBlockBreakPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SBlockBreakingPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SChunkStatusPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SPlaceBlockPacket;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.ChunkVecSpace;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.badlogic.gdx.math.MathUtils.lerp;
import static java.lang.Math.max;

@SuppressWarnings("GDXJavaUnsafeIterator")
public final class ClientWorld extends World implements Disposable, ClientWorldAccess {
    public static final int DAY_CYCLE = 24000;

    public static final AtomicReference<RgbColor> FOG_COLOR = new AtomicReference<>(RgbColor.rgb(0x7fb0fe));
    public static final AtomicReference<Vec2f> ATLAS_SIZE = new AtomicReference<>(new Vec2f(2048, 2048));
    public static final AtomicDouble FOG_DENSITY = new AtomicDouble(0.001);
    public static final AtomicDouble FOG_START = new AtomicDouble(0.0);
    public static final AtomicDouble FOG_END = new AtomicDouble(1.0);

    public static final Color DAY_TOP_COLOR = new Color(0x7fb0feff);
    public static final Color DAY_BOTTOM_COLOR = new Color(0xc1d3f1ff);
    public static final Color NIGHT_TOP_COLOR = new Color(0x01010bff);
    public static final Color NIGHT_BOTTOM_COLOR = new Color(0x0a0c16ff);
    public static final Color SUN_RISE_COLOR = new Color(0xff3000ff);
    public static final Color VOID_COLOR = new Color(0x0a0a0aff);

    public static final AtomicReference<Vec2f> ATLAS_OFFSET = new AtomicReference<>(new Vec2f(1 + 1 - (ATLAS_SIZE.get().x / (ATLAS_SIZE.get().x)), ATLAS_SIZE.get().y));

    public static Rot SKYBOX_ROTATION = Rot.deg(-60);
    public static int VOID_Y_START = 20;
    public static int VOID_Y_END = 0;
    @NotNull
    private final QuantumClient client;
    private final RegistryKey<DimensionInfo> dimension;
    private final Map<ChunkVec, ClientChunk> chunks = new ConcurrentHashMap<>();
    private int chunkRefresh;
    private ChunkVec oldChunkVec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
    private long time = 0;
    private int totalChunks;
    private final Vec3i tmp = new Vec3i();
    private final Queue<LightData> panelQueue = new Queue<>();
    private final ObjectIntMap<LightData> panelMap = new ObjectIntMap<>();

    private final IntMap<Gizmo> entityGizmos = new IntMap<>();

    private final ObjectMap<String, Array<Gizmo>> gizmos = new ObjectMap<>();
    private final ObjectSet<String> enabledCategories = new ObjectSet<>();

    /**
     * Constructor for creating an instance of ClientWorld.
     *
     * @param client    an instance of QuantumClient which interacts with the client-side quantum framework.
     * @param dimension a RegistryKey of DimensionInfo representing the dimension of the world.
     */
    public ClientWorld(@NotNull QuantumClient client, RegistryKey<DimensionInfo> dimension) {
        super();
        this.client = client;
        this.dimension = dimension;
    }

    public void toggleGizmoCategory(String category) {
        if (enabledCategories.contains(category)) enabledCategories.remove(category);
        else enabledCategories.add(category);
    }

    public void addGizmo(Gizmo gizmo) {
        String category = gizmo.category;
        if (!this.gizmos.containsKey(category)) {
            this.gizmos.put(category, new Array<>(new Gizmo[]{gizmo}));
        } else {
            this.gizmos.get(category).add(gizmo);
        }
    }

    public void removeGizmo(Gizmo gizmo) {
        String category = gizmo.category;
        var gizmos = this.gizmos.get(category);
        if (gizmos != null) {
            gizmos.removeValue(gizmo, true);
        }
    }

    public Gizmo[] getGizmos(String category) {
        return gizmos.get(category, new Array<>()).toArray(Gizmo.class);
    }

    public String[] getGizmoCategories() {
        return gizmos.keys().toArray().toArray(String.class);
    }

    public boolean isGimzoCategoryEnabled(String category) {
        return enabledCategories.contains(category);
    }

    public ObjectSet<String> getEnabledGizmoCategories() {
        return enabledCategories;
    }

    @Override
    public int getRenderDistance() {
        return ClientConfig.renderDistance / CHUNK_SIZE;
    }

    /**
     * Unloads a chunk from the world.
     *
     * @param chunk The chunk to unload.
     * @param pos   The position of the chunk.
     * @return True if the chunk was successfully unloaded, false otherwise.
     */
    @Override
    public boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkVec pos) {
        if (!(chunk instanceof ClientChunk clientChunk))
            throw new IllegalArgumentException("Chunk must be a ClientChunk");

        // Check if the chunk should stay loaded
        if (shouldStayLoaded(pos)) {
            // If it should, return false
            return false;
        }

        // Try to remove the chunk from the chunk map
        synchronized (this.chunks){
            ClientChunk removedChunk = this.chunks.remove(pos);
            boolean removed = removedChunk != null;
            if (removed) {
                // If the chunk was removed, decrement the total number of chunks
                TerrainRenderer worldRenderer = this.client.worldRenderer;
                if (worldRenderer != null)
                    QuantumClient.invoke(() -> worldRenderer.unload(clientChunk));

                if (removedChunk != chunk)
                    LOGGER.warn("Removed chunk mismatch: {} != {}", removedChunk, chunk);

                this.totalChunks--;
                removedChunk.dispose();
            }

            // Return true if the chunk was removed, false otherwise
            return removed;
        }
    }

    @Override
    protected void checkThread() {

    }

    @Override
    public @Nullable ClientChunk getChunk(@NotNull ChunkVec pos) {
        synchronized (this.chunks) {
            return this.chunks.get(pos);
        }
    }

    @Override
    public ClientChunk getChunk(int x, int y, int z) {
        return (ClientChunk) super.getChunk(x, y, z);
    }

    @Override
    public @Nullable ClientChunk getChunkAt(@NotNull BlockVec pos) {
        return (ClientChunk) super.getChunkAt(pos);
    }

    @Override
    public ClientChunk getChunkAt(int x, int y, int z) {
        return (ClientChunk) super.getChunkAt(x, y, z);
    }

    @Override
    public Collection<ClientChunk> getLoadedChunks() {
        return this.chunks.values();
    }

    @Override
    public boolean isChunkInvalidated(@NotNull Chunk chunk) {
        return super.isChunkInvalidated(chunk);
    }

    @Override
    public void updateChunkAndNeighbours(@NotNull Chunk chunk) {
        super.updateChunkAndNeighbours(chunk);

//        this.updateLightChunks((ClientChunk) chunk);
    }

    @Override
    public void updateChunk(@Nullable Chunk chunk) {
        if (chunk == null) return;
        if (!QuantumClient.isOnRenderThread()) {
            QuantumClient.invokeAndWait(() -> this.updateChunk(chunk));
            return;
        }

        if (!(chunk instanceof ClientChunk clientChunk))
            throw new IllegalArgumentException("Chunk must be a ClientChunk but was " + chunk.getClass().getSimpleName());

        clientChunk.markNotEmpty();

        if (this.client.worldRenderer instanceof WorldRenderer worldRenderer) {
            worldRenderer.rebuild(clientChunk);
        }

        super.updateChunk(chunk);
    }

    @Override
    public void startBreaking(@NotNull BlockVec breaking, @NotNull Player breaker) {
        if (breaker == this.client.player) {
            this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.START));
        }
        super.startBreaking(breaking, breaker);
    }

    /**
     * Continues breaking a block at the specified position by the given amount.
     *
     * @param breaking the position of the block being broken.
     * @param amount   the amount of breaking progress to be made.
     * @param breaker  the player breaking the block.
     * @return the result of the block breaking operation.
     */
    @Override
    public BreakResult continueBreaking(@NotNull BlockVec breaking, float amount, @NotNull Player breaker) {
        // Check if the breaker is the current player
        if (breaker == this.client.player) {
            // Send a packet to continue breaking the block
            this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.CONTINUE));
        }

        // Call the superclass method to continue breaking the block
        BreakResult breakResult = super.continueBreaking(breaking, amount, breaker);

        // Check if the block has been fully broken
        if (breakResult == BreakResult.BROKEN) {
            // Attempt to destroy the block and send appropriate packets
            if (this.destroyBlock(breaking, breaker)) {
                this.client.connection.send(new C2SBlockBreakPacket(breaking));
            }
        }

        return breakResult;
    }

    /**
     * Stops the block-breaking process for a specified player at a given block location.
     *
     * @param breaking The block location where the breaking process is to be stopped.
     * @param breaker  The player who is attempting to stop breaking the block.
     * @return true if the stop breaking process was successful, false otherwise.
     */
    @Override
    public boolean stopBreaking(@NotNull BlockVec breaking, @NotNull Player breaker) {
        // Check if the breaker is the local player
        if (breaker == this.client.player) {
            // Send a packet to stop the breaking process
            this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.STOP));
        }

        return super.stopBreaking(breaking, breaker);
    }

    /**
     * Triggered when a chunk is updated.
     *
     * @param chunk the updated chunk.
     */
    @Override
    public void onChunkUpdated(@NotNull Chunk chunk) {
        super.onChunkUpdated(chunk);
    }

    /**
     * Plays a sound at a specific position.
     *
     * @param sound The sound event to be played.
     * @param x     The setX position of the sound.
     * @param y     The setY position of the sound.
     * @param z     The z position of the sound.
     */
    @Override
    public void playSound(@NotNull SoundEvent sound, double x, double y, double z) {
        // Get the range of the sound
        float range = sound.getRange();

        // Get the player associated with the client
        Player player = this.client.player;

        // If the player exists, calculate the distance between the player and the sound position
        // and play the sound with the calculated volume
        if (player != null) {
            float distance = (float) player.getPosition().dst(x, y, z);
            float volume = (range - distance) / range;
            player.playSound(sound, volume);
        }
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public boolean set(int x, int y, int z, @NotNull BlockState block, int flags) {
        // Check if we're on the main thread, if not invokeAndWait the method on the main thread
        if (!QuantumClient.isOnRenderThread()) {
            return QuantumClient.invokeAndWait(() -> this.set(x, y, z, block, flags));
        }

        // Set the block and get the result
        boolean isBlockSet = super.set(x, y, z, block, flags);

        // Get the chunk containing the block
        BlockVec blockVec = new BlockVec(x, y, z, BlockVecSpace.WORLD);
        ClientChunk chunk = this.getChunkAt(blockVec);

        // If the chunk exists, set the light source
        if (chunk != null) {
            chunk.setLightSource(World.toLocalBlockVec(x, y, z, this.tmp), block.getLight());
        }

        // If the SYNC flag is set, sync the block
        if ((flags & BlockFlags.SYNC) != 0) {
            this.sync(x, y, z, block);
        }

        // If the LIGHT flag is set and the chunk exists, update the light chunks
        if ((flags & BlockFlags.LIGHT) != 0 && chunk != null && isBlockSet) {
//            this.updateLightChunks(chunk);
        }

        // If the UPDATE flag is set and the chunk exists, update the chunk and its neighbors
        if ((flags & BlockFlags.UPDATE) != 0 && chunk != null && isBlockSet) {
            chunk.set(World.toLocalBlockVec(x, y, z, this.tmp), block);
            this.updateChunkAndNeighbours(chunk);

            // Update the blocks in each direction of the block
            for (Direction direction : Direction.values()) {
                BlockVec offset = blockVec.offset(direction);
                BlockState blockState = this.get(offset);
                blockState.update(this, offset);
            }
        }

        return isBlockSet;
    }

    /**
     * Sets the block at the specified position to the given block properties.
     *
     * @param pos The coordinates of the block position.
     * @param block The block properties to set.
     * @param flags Flags indicating how the block should be set.
     * @return True if the block was successfully set, false otherwise.
     */
    @Override
    public boolean set(@NotNull BlockVec pos, @NotNull BlockState block, int flags) {
        return set(pos.x, pos.y, pos.z, block, flags);
    }

    /**
     * Updates the light in the current chunk and its neighboring chunks.
     *
     * @param chunk The chunk for which to update the light.
     */
    private void updateLightChunks(ClientChunk chunk) {
        // Get the neighboring chunks of the current chunk
        ClientChunk[] neighbourChunks = this.getNeighbourChunks(chunk);

        // Clear light in the current chunk
        chunk.clearLight();

        // Clear light in each neighboring chunk
        for (ClientChunk neighbourChunk : neighbourChunks) {
            if (neighbourChunk != null)
                neighbourChunk.clearLight();
        }

        // Update light in the current chunk
        this.updateLightChunk(chunk);

        // Update light in each neighboring chunk
        for (ClientChunk neighbourChunk : neighbourChunks) {
            if (neighbourChunk != null)
                this.updateLightChunk(neighbourChunk);
        }
    }

    private ClientChunk[] getNeighbourChunks(ClientChunk chunk) {
        ClientChunk[] neighbourChunks = new ClientChunk[26];
        ChunkVec vec = chunk.getVec();

        int index = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    int chunkX = vec.x + x;
                    int chunkY = vec.y + y;
                    int chunkZ = vec.z + z;

                    neighbourChunks[index++] = this.getChunk(chunkX, chunkY, chunkZ);
                }
            }
        }

        return neighbourChunks;
    }

    private void updateLightChunk(ClientChunk chunk) {
        if (client.world != null) {
            chunk.updateLight(client.world);
        }

        BlockVec offset = chunk.getOffset();
        for (int x = offset.x; x < offset.x + CHUNK_SIZE; x++) {
            for (int z = offset.x; z < offset.x + CHUNK_SIZE; z++) {
                setInitialSunlight(x, z);
            }
        }
    }

    public void setInitialSunlight(int startX, int startZ) {
        Queue<int[]> queue = new Queue<>();

        int startY = 256;
        Chunk chunk = getChunkAt(startX, startY, startZ);
        if (chunk == null) return;

        // Start from the top of the world and move downward
        for (int y = 256 - 1; y >= 0; y--) {
            BlockVec localBlockVec = new BlockVec(startX, y, startZ, BlockVecSpace.WORLD);
            int lightReduction = chunk.get(new BlockVec(startX, y, startZ, BlockVecSpace.WORLD).chunkLocal()).getLightReduction();
            if (lightReduction < 15) {
                int intensity = 15 - lightReduction;
                setSunlight(localBlockVec.x, localBlockVec.y, localBlockVec.z, intensity); // Assuming maximum sunlight intensity is 15
                queue.addLast(new int[]{localBlockVec.x, localBlockVec.y, localBlockVec.z, intensity});
            } else {
                break; // Stop when hitting a non-transparent block
            }
        }

        // Process the queue to propagate sunlight horizontally and downward
        while (!queue.isEmpty()) {
            int[] current = queue.removeFirst();
            int x = chunk.getOffset().x + current[0];
            int y = chunk.getOffset().y + current[1];
            int z = chunk.getOffset().z + current[2];

            int currentLight = getSunlight(x, y, z);

            if (currentLight > 1) {
                int newLight = currentLight - 1;
                int reduction = getReduction(x, y, z);

                // Check and propagate to neighboring blocks
                if (!(currentLight == 15 && current[0] == 15))
                    propagateSunlight(queue, x + 1, y, z, newLight, reduction);
                if (!(currentLight == 15 && current[0] == 0))
                    propagateSunlight(queue, x - 1, y, z, newLight, reduction);
                if (!(currentLight == 15 && current[2] == 15))
                    propagateSunlight(queue, x, y, z + 1, newLight, reduction);
                if (!(currentLight == 15 && current[2] == 0))
                    propagateSunlight(queue, x, y, z - 1, newLight, reduction);

                // Check the block below to propagate sunlight downward
                if (y > 0 && getReduction(x, y - 1, z) < 15) {
                    propagateSunlight(queue, x, y - 1, z, newLight, reduction);
                }
            }
        }
    }

    private int getReduction(int x, int y, int z) {
        return max(get(x, y, z).getLightReduction(), 0);
    }

    private void propagateSunlight(Queue<int[]> queue, int x, int y, int z, int intensity, int reduction) {
        if (y >= 0 && y < 256 && reduction < 15 && getSunlight(x, y, z) < intensity) {
            int i = intensity - reduction;
            if (i < 0) return;
            setSunlight(x, y, z, intensity);
            queue.addLast(new int[]{x, y, z, i});
        }
    }

    public int getSunlight(int x, int y, int z) {
        ClientChunk chunk = getChunkAt(x, y, z);
        if (chunk != null) {
            return chunk.getSunlight(toLocalBlockVec(x, y, z));
        }

        return 0;
    }

    private void setSunlight(int x, int y, int z, int intensity) {
        ClientChunk chunk = getChunkAt(x, y, z);

        if (chunk != null) {
            chunk.setSunlight(toLocalBlockVec(x, y, z), intensity);
        }
    }

    public void resetQueue() {
        panelQueue.clear();
        panelMap.clear();
    }

    /**
     * Fills a region with light starting from a given position.
     * Uses a breadth-first search algorithm to traverse the region.
     *
     * @param startX The x-coordinate of the starting position.
     * @param startY The y-coordinate of the starting position.
     * @param startZ The z-coordinate of the starting position.
     * @param light  The initial light intensity.
     */
    private void floodfill(int startX, int startY, int startZ, int light) {
        // Initialize a queue to store the positions to be processed
        Queue<int[]> queue = new Queue<>();
        queue.addLast(new int[]{startX, startY, startZ, light});

        // Continue processing until the queue is empty
        while (!queue.isEmpty()) {
            // Remove the next position from the queue
            int[] current = queue.removeFirst();
            int x = current[0];
            int y = current[1];
            int z = current[2];
            int intensity = current[3];

            // If the current block light is less than the intensity, update the block light
            if (getBlockLight(x, y, z) < intensity) {
                setBlockLight(x, y, z, intensity);
            } else {
                // Skip processing if the current block light is not less than the intensity
                continue;
            }

            // If there is still light to propagate, add the neighboring positions to the queue
            if (intensity > 0) {
                newState(queue, x + 1, y, z, intensity);
                newState(queue, x, y + 1, z, intensity);
                newState(queue, x, y, z + 1, intensity);
                newState(queue, x - 1, y, z, intensity);
                newState(queue, x, y - 1, z, intensity);
                newState(queue, x, y, z - 1, intensity);
            }
        }
    }

    /**
     * Adds a new position to the queue to be processed in the floodfill algorithm.
     *
     * @param queue     The queue of positions to be processed.
     * @param x         The x-coordinate of the new position.
     * @param y         The y-coordinate of the new position.
     * @param z         The z-coordinate of the new position.
     * @param intensity The light intensity of the new position.
     */
    private void newState(Queue<int[]> queue, int x, int y, int z, int intensity) {
        ClientChunk chunkAt = this.getChunkAt(x, y, z);
        if (chunkAt == null) return;
        BlockState blockProperties = chunkAt.get(toLocalBlockVec(x, y, z));
        int lightReduction = max(blockProperties.getLightReduction(), 1);
        queue.addLast(new int[]{x, y, z, intensity - lightReduction});
    }

    public int getBlockLight(Vec3i pos) {
        return getBlockLight(pos.x, pos.y, pos.z);
    }

    public void setBlockLight(LightData data, int level) {
        this.setBlockLight(data.pos, level);
    }

    public void setBlockLight(Vec3i pos, int level) {
        this.setBlockLight(pos.x, pos.y, pos.z, level);
    }

    /**
     * Gets the neighboring light data for a given light data.
     *
     * @param data the light data for which neighbors are needed
     * @return an array of neighboring light data
     */
    public LightData[] getNeighbors(LightData data) {
        LightData[] neighbors = new LightData[6];

        // Right / left neighbor
        neighbors[0] = getBlockLightData(data.pos.x + 1, data.pos.y, data.pos.z);
        neighbors[1] = getBlockLightData(data.pos.x - 1, data.pos.y, data.pos.z);

        // Top / bottom neighbor
        neighbors[2] = getBlockLightData(data.pos.x, data.pos.y + 1, data.pos.z);
        neighbors[3] = getBlockLightData(data.pos.x, data.pos.y - 1, data.pos.z);

        // Front / back neighbor
        neighbors[4] = getBlockLightData(data.pos.x, data.pos.y, data.pos.z + 1);
        neighbors[5] = getBlockLightData(data.pos.x, data.pos.y, data.pos.z - 1);

        return neighbors;
    }

    private LightData getBlockLightData(int x, int y, int z) {
        return new LightData(x, y, z, (byte) getBlockLight(x, y, z));
    }

    @Override
    public int getBlockLight(int x, int y, int z) {
        ClientChunk chunk = getChunkAt(x, y, z);
        if (chunk != null) {
            return (byte) chunk.getBlockLight(toLocalBlockVec(x, y, z, this.tmp));
        } else {
            return 0;
        }
    }

    @Override
    public void setBlockLight(int x, int y, int z, int light) {
        ClientChunk chunk = this.getChunkAt(x, y, z);
        if (chunk != null) {
            chunk.setBlockLight(toLocalBlockVec(x, y, z, this.tmp), light);
        }
    }

    /**
     * Updates the light sources in the world at a given offset with the provided lights.
     *
     * @param offset The offset at which the light sources are updated.
     * @param lights A map of light sources to be updated.
     */
    @Override
    public void updateLightSources(@NotNull Vec3i offset, @NotNull ObjectMap<Vec3i, LightSource> lights) {
        // Call the superclass method to update light sources
        super.updateLightSources(offset, lights);

        // Update sunlight for each block in the chunk
        for (int x = offset.x; x < offset.x + CHUNK_SIZE; x++) {
            for (int z = offset.z; z < offset.z + CHUNK_SIZE; z++) {
                setInitialSunlight(x, z);
            }
        }

        // Process each light source and perform floodfill algorithm
        for (LightSource source : lights.values().toArray()) {
            int x = offset.x + source.x();
            int y = offset.y + source.y();
            int z = offset.z + source.z();
            int level = source.level();

            // Fill the region with light starting from the source position
            floodfill(x, y, z, level);
        }
    }

    @Override
    public boolean isLoaded(@NotNull Chunk chunk) {
        if (chunk instanceof ClientChunk clientChunk) {
            return this.chunks.containsValue(clientChunk);
        }

        return false;
    }

    private void updateSunlight(int blockX, int blockY, int blockZ, boolean isAdded) {
        if (blockY >= 256) return;

        Queue<int[]> queue = new Queue<>();

        if (isAdded) {
            // Block is added, set its sunlight to 0 and start the queue
            queue.addLast(new int[]{blockX, blockY, blockZ});
            setSunlight(blockX, blockY, blockZ, 0);
        } else {
            // Block is removed, propagate sunlight downward from the removed block's position
            for (int y = blockY; y >= 0; y--) {
                int reduction = getReduction(blockX, y, blockZ);
                if (reduction < 15) {
                    int newLight = getSunlight(blockX, y + 1, blockZ) - reduction;
                    setSunlight(blockX, y, blockZ, newLight);
                    queue.addLast(new int[]{blockX, y, blockZ, newLight});
                } else {
                    break;
                }
            }
        }

        // Process the queue to update sunlight propagation
        while (!queue.isEmpty()) {
            // Remove the first id from the queue and get its position and light information
            int[] current = queue.removeFirst();
            int x = current[0];
            int y = current[1];
            int z = current[2];
            int currentLight = getSunlight(x, y, z);
            int reduction = getReduction(x, y, z);

            // Update sunlight for neighboring blocks
            updateNeighborSunlight(queue, x + 1, y, z, currentLight, reduction);
            updateNeighborSunlight(queue, x - 1, y, z, currentLight, reduction);
            updateNeighborSunlight(queue, x, y, z + 1, currentLight, reduction);
            updateNeighborSunlight(queue, x, y, z - 1, currentLight, reduction);

            // Update sunlight for the block below if it exists
            if (y > 0) {
                updateNeighborSunlight(queue, x, y - 1, z, currentLight, reduction);
            }

            // Update sunlight for the block above if it is transparent and within world height limits
            if (y < 256 - 1 && reduction < 15) {
                updateNeighborSunlight(queue, x, y + 1, z, currentLight, reduction);
            }
        }
    }

    /**
     * Updates the sunlight of a neighboring block and adds it to the queue.
     *
     * @param queue        The queue to add the block to.
     * @param x            The setX-coordinate of the block.
     * @param y            The setY-coordinate of the block.
     * @param z            The z-coordinate of the block.
     * @param currentLight The current sunlight of the block.
     * @param reduction    The amount of sunlight to reduce.
     */
    private void updateNeighborSunlight(Queue<int[]> queue, int x, int y, int z, int currentLight, int reduction) {
        // Check if the block is within the world height limits
        if (y >= 0 && y < 256) {
            // Get the current sunlight of the neighboring block
            int neighborLight = getSunlight(x, y, z);

            // Calculate the reduced sunlight
            int reduced = currentLight - reduction;

            // Check if the neighboring block has sunlight and if it is less than the current block
            if (neighborLight != 0 && neighborLight < currentLight) {
                // Calculate the maximum sunlight between the neighboring block and the reduced sunlight
                int max = Math.max(neighborLight, Math.max(reduced, 0));

                // Set the sunlight of the neighboring block
                setSunlight(x, y, z, max);

                // Add the block to the queue
                queue.addLast(new int[]{x, y, z, max});
            } else if (neighborLight > currentLight) {
                // If the neighboring light is greater than the current block light, reduce it
                setSunlight(x, y, z, reduced);

                // Add the block to the queue
                queue.addLast(new int[]{x, y, z, reduced});
            }
        }
    }

    @Override
    @ApiStatus.Experimental
    public void spawnParticles(@NotNull ParticleType particleType, @NotNull Vec3d position, @NotNull Vec3d motion, int count) {
//        if (!QuantumClient.isOnRenderThread()) {
//            QuantumClient.invokeAndWait(() -> this.spawnParticles(particleType, position, motion, count));
//        }
//
//        super.spawnParticles(particleType, position, motion, count);
//
//        ClientParticle clientParticle = ClientParticleRegistry.getParticle(particleType);
//        WorldRenderer worldRenderer = this.client.worldRenderer;
//        if (worldRenderer != null && clientParticle != null) {
//            PFXPool particleController = clientParticle.getPool();
//            ParticleEffect obtained = particleController.obtain();
//            worldRenderer.addParticles(obtained, position, motion, count);
//        } else if (clientParticle == null) {
//            World.LOGGER.warn("Unknown particle type: {}", particleType.getId());
//        }
    }

    private void sync(int x, int y, int z, BlockState block) {
        if (block.isAir()) return;
        this.client.connection.send(new C2SPlaceBlockPacket(x, y, z, block));
    }

    /**
     * Loads a chunk into the client world.
     *
     * @param pos  The position of the chunk.
     * @param data The data for the chunk.
     */
    public void loadChunk(ChunkVec pos, ClientChunk data) {
        // Get the current chunk at the given position
        ClientChunk chunk;
        synchronized (this.chunks) {
            chunk = this.chunks.get(pos);
        }

        // If the chunk doesn't exist, set it to the new data
        if (chunk == null) {
            chunk = data;
        } else {
            // If the chunk already exists, log a warning and return
            unloadChunk(chunk, pos);
            return;
        }

        // Get the local player
        LocalPlayer player = this.client.player;

        // If the player is null, send a failed chunk status packet and return
        if (player == null) {
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
            return;
        }

        QuantumClient.invoke(() -> {
            BoxGizmo gizmo = new BoxGizmo("chunk");
            gizmo.position.set(data.getOffset().vec().d().add(8.0, 8.0, 8.0));
            gizmo.size.set(CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE);
            gizmo.color.set(1.0F, 0.0F, 0.0F, 1.0F);
            gizmo.outline = true;
            this.addGizmo(gizmo);
        });

        // Calculate the distance between the chunk and the player
        synchronized (this.chunks) {
            if (pos.dst(player.getChunkVec()) > ClientConfig.renderDistance / CHUNK_SIZE) {
                player.pendingChunks.remove(pos);

                // If the distance is greater than the render distance, send a skip chunk status packet and return
                this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.UNLOADED));
                return;
            }

            QuantumClient.invoke(() -> player.pendingChunks.remove(pos));

            // Add the chunk to the map of chunks
            this.chunks.put(pos, chunk);
            // Increment the total number of chunks
            this.totalChunks++;
            // Mark the chunk as ready
            chunk.ready();
            // Send a success chunk status packet
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SUCCESS));
        }
    }

    public Gizmo getEntityGizmo(Entity entity) {
        return this.entityGizmos.get(entity.getId());
    }

    /**
     * Tick method to update the state of the ClientWorld.
     * It increments the time and checks if the chunk refresh is due.
     * If the chunk refresh is due, it updates the chunks based on the player's position.
     */
    public void tick() {
        // Increment the time
        this.time++;

        // Check if the chunk refresh is due
        if (this.chunkRefresh-- <= 0) {
            // Reset the chunk refresh counter
            this.chunkRefresh = 40;

            // Get the local player
            LocalPlayer player = this.client.player;

            // If the player is not null
            if (player != null) {
                this.tickLocalPlayer(player);
            }
        }
    }

    private void tickLocalPlayer(LocalPlayer player) {
        // Check if the old chunk position is the same as the current player's chunk position
        if (this.oldChunkVec.equals(player.getChunkVec())) {
            return;
        }

        // Update the old chunk position with the current player's chunk position
        this.oldChunkVec = player.getChunkVec();

        // Iterate over the chunks
        for (Iterator<Map.Entry<ChunkVec, ClientChunk>> iterator = this.chunks.entrySet().iterator(); iterator.hasNext(); ) {
            // Get the chunk entry
            Map.Entry<ChunkVec, ClientChunk> entry = iterator.next();
            ChunkVec chunkVec = entry.getKey();
            ClientChunk clientChunk = entry.getValue();

            // Check if the distance between the chunk and the player's position is greater than the render distance
            if (new Vec2d(chunkVec.getIntX(), chunkVec.getIntZ()).dst(player.getChunkVec().getIntX(), player.getChunkVec().getIntZ()) > ClientConfig.renderDistance / CHUNK_SIZE) {
                // Remove the chunk from the map and dispose it
                iterator.remove();
                clientChunk.dispose();

                // Update the neighbours of the chunk
                this.updateNeighbours(clientChunk);
            }
        }
    }

    public Array<Entity> getAllEntities() {
        return this.entitiesById.values().toArray();
    }

    public float getGlobalSunlight() {
        long daytime = this.getDaytime();
        final int riseSetDuration = ClientWorld.DAY_CYCLE / 24;
        if (daytime < riseSetDuration / 2) {
            return lerp(
                    0.25f, 1.0f,
                    0.5f + daytime / (float) riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 - riseSetDuration / 2) {
            return 1.0f;
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 + riseSetDuration / 2) {
            return lerp(
                    1.0f, 0.25f,
                    (daytime - ((float) ClientWorld.DAY_CYCLE / 2 - (float) riseSetDuration / 2)) / riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE - riseSetDuration / 2) {
            return 0.25f;
        } else {
            return lerp(
                    0.25f, 1.0f,
                    (daytime - (ClientWorld.DAY_CYCLE - (float) riseSetDuration / 2)) / riseSetDuration);
        }
    }

    @Override
    public boolean isLoaded(ChunkVec chunkVec) {
        return this.chunks.containsKey(chunkVec);
    }

    @Override
    public void onBlockSet(BlockVec pos, BlockState block) {
        @Nullable ClientChunk chunkAt = this.getChunkAt(pos);
        if (chunkAt == null) return;
        chunkAt.set(pos.chunkLocal(), block);

        this.updateChunkAndNeighbours(chunkAt);
    }

    public long getDaytime() {
        return this.time % DAY_CYCLE;
    }

    static Color mixColors(Color color1, Color color2, Color output, double percent) {
        percent = Mth.clamp(percent, 0.0, 1.0);
        double inversePercent = 1.0 - percent;
        int redPart = (int) (color1.r * percent + color2.r * inversePercent);
        int greenPart = (int) (color1.g * percent + color2.g * inversePercent);
        int bluePart = (int) (color1.b * percent + color2.b * inversePercent);
        int alphaPart = (int) (color1.a * percent + color2.a * inversePercent);
        return output.set(redPart, greenPart, bluePart, alphaPart);
    }

    @Override
    public void dispose() {
        super.dispose();

        synchronized (this.chunks) {
            this.chunks.forEach((ChunkVec, clientChunk) -> clientChunk.dispose());
            this.chunks.clear();
        }
    }

    @Override
    public int getTotalChunks() {
        return this.totalChunks;
    }

    public void setDaytime(long daytime) {
        this.time = daytime;
    }

    public void addEntity(int id, EntityType<?> type, Vec3d position, MapType pipeline) {
        QuantumClient.LOGGER.debug("Adding entity with id " + id + " of type " + type.getId() + " at " + position);

        Entity entity = type.create(this);
        entity.setId(id);
        entity.setPosition(position);
        entity.onPipeline(pipeline);
        BoxGizmo boxGizmo = new BoxGizmo("entity-bounds");
        boxGizmo.position.set(position);
        boxGizmo.size.set(entity.getSize().width(), entity.getSize().height(), entity.getSize().width());
        boxGizmo.outline = true;
        this.entityGizmos.put(id, boxGizmo);
        this.addGizmo(boxGizmo);
        this.entitiesById.put(id, entity);
    }

    @CanIgnoreReturnValue
    public Entity removeEntity(int id) {
        Entity remove = this.entitiesById.remove(id);
        this.removeGizmo(this.entityGizmos.remove(id));
        @Nullable TerrainRenderer worldRenderer = client.worldRenderer;
        if (worldRenderer != null) {
            worldRenderer.removeEntity(id);
        }
        return remove;
    }

    /**
     * Handles the attack action initiated by a player.
     *
     * @param playerId The ID of the player initiating the attack.
     * @param entityId The ID of the entity being attacked.
     */
    public void onPlayerAttack(int playerId, int entityId) {
        // Get the player entity and the target entity based on their IDs
        Entity player = this.entitiesById.get(playerId);
        Entity entity = this.entitiesById.get(entityId);

        // If the target entity exists and the player is a remote player, trigger the attack
        if (entity != null && player instanceof RemotePlayer remotePlayer) {
            remotePlayer.onAttack(entity);
        }

        // This should not happen, as only remote players should trigger attacks
        if (player instanceof LocalPlayer) {
            // Log a warning if a local player tries to attack (sanity check)
            LOGGER.warn("SANITY CHECK: local player tried to attack entity {}!", entityId);
        }
    }

    @Override
    public RegistryKey<DimensionInfo> getDimension() {
        return dimension;
    }
}
