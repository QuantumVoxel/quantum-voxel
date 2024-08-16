package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.utils.*;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.libs.commons.v0.vector.Vec2d;
import dev.ultreon.libs.commons.v0.vector.Vec2f;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
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
import dev.ultreon.quantum.util.InvalidThreadException;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.badlogic.gdx.math.MathUtils.lerp;
import static dev.ultreon.quantum.client.util.ExtKt.deg;
import static java.lang.Math.max;

public final class ClientWorld extends World implements Disposable, ClientWorldAccess {
    public static final int DAY_CYCLE = 24000;
    public static final AtomicReference<RgbColor> FOG_COLOR = new AtomicReference<>(RgbColor.rgb(0x7fb0fe));
    public static final AtomicDouble FOG_DENSITY = new AtomicDouble(0.001);
    public static final AtomicDouble FOG_START = new AtomicDouble(0.0);
    public static final AtomicDouble FOG_END = new AtomicDouble(1.0);
    public static final AtomicReference<Vec2f> ATLAS_SIZE = new AtomicReference<>(new Vec2f(2048, 2048));
    //                                                                           off.get().set(f(1 - 1 / size.get().x * size.get().x * 2 / 87), 1)
    public static final AtomicReference<Vec2f> ATLAS_OFFSET = new AtomicReference<>(new Vec2f((float) (1 + 1 - (ATLAS_SIZE.get().x / (ATLAS_SIZE.get().x - (7.5 * 6.128)))), 1 - (1 - 1.03125f) / 256 * ATLAS_SIZE.get().y));
    public static Rot SKYBOX_ROTATION = deg(-60);
    public static RgbColor DAY_TOP_COLOR = RgbColor.rgb(0x7fb0fe);
    public static RgbColor DAY_BOTTOM_COLOR = RgbColor.rgb(0xc1d3f1);
    public static RgbColor NIGHT_TOP_COLOR = RgbColor.rgb(0x01010b);
    public static RgbColor NIGHT_BOTTOM_COLOR = RgbColor.rgb(0x0a0c16);
    public static RgbColor SUN_RISE_COLOR = RgbColor.rgb(0xff3000);
    public static RgbColor VOID_COLOR = RgbColor.rgb(0x0a0a0a);
    public static int VOID_Y_START = 20;
    public static int VOID_Y_END = 0;
    @NotNull
    private final QuantumClient client;
    private final Map<ChunkVec, ClientChunk> chunks = new HashMap<>();
    private int chunkRefresh;
    private ChunkVec oldChunkVec = new ChunkVec(0, 0);
    private int time = 0;
    private int totalChunks;
    private final Vec3i tmp = new Vec3i();
    private final Queue<LightData> panelQueue = new Queue<>();
    private final ObjectIntMap<LightData> panelMap = new ObjectIntMap<>();

    /**
     * Constructs a new ClientWorld object.
     *
     * @param client The QuantumClient object that this ClientWorld is associated with.
     */
    public ClientWorld(@NotNull QuantumClient client) {
        super();
        this.client = client;
    }

    @Override
    public int getRenderDistance() {
        return ClientConfig.renderDistance;
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
        // Check if the current thread is the main thread
        if (!QuantumClient.isOnRenderThread()) {
            // If not, invoke the unloadChunk method on the main thread and return the result
            return QuantumClient.invokeAndWait(() -> this.unloadChunk(chunk, pos));
        }

        if (!(chunk instanceof ClientChunk clientChunk)) {
            throw new IllegalArgumentException("Chunk must be a ClientChunk");
        }

        // Check if the chunk should stay loaded
        if (shouldStayLoaded(pos)) {
            // If it should, return false
            return false;
        }

        // Try to remove the chunk from the chunks map
        ClientChunk removedChunk = this.chunks.remove(pos);
        boolean removed = removedChunk != null;
        if (removed) {
            // If the chunk was removed, decrement the total number of chunks
            TerrainRenderer worldRenderer = this.client.worldRenderer;
            if (worldRenderer != null) worldRenderer.unload(clientChunk);
            if (removedChunk != chunk) {
                LOGGER.warn("Removed chunk mismatch: {} != {}", removedChunk, chunk);
            }

            this.totalChunks--;
        }

        // Return true if the chunk was removed, false otherwise
        return removed;
    }

    @Override
    protected void checkThread() {
        if (!QuantumClient.isOnRenderThread())
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
    }

    @Override
    public @Nullable ClientChunk getChunk(@NotNull ChunkVec pos) {
        synchronized (this) {
            return this.chunks.get(pos);
        }
    }

    @Override
    public ClientChunk getChunk(int x, int z) {
        return (ClientChunk) super.getChunk(x, z);
    }

    @Override
    public ClientChunk getChunkAt(@NotNull BlockVec pos) {
        return (ClientChunk) super.getChunkAt(pos);
    }

    @Override
    public ClientChunk getChunkAt(int x, int y, int z) {
        return (ClientChunk) super.getChunkAt(x, y, z);
    }

    @Override
    public Collection<ClientChunk> getLoadedChunks() {
        this.checkThread();

        return this.chunks.values();
    }

    @Override
    public boolean isChunkInvalidated(@NotNull Chunk chunk) {
        this.checkThread();
        return super.isChunkInvalidated(chunk);
    }

    @Override
    public void updateChunkAndNeighbours(@NotNull Chunk chunk) {
        super.updateChunkAndNeighbours(chunk);

        this.updateLightChunks((ClientChunk) chunk);
    }

    @Override
    public void updateChunk(@Nullable Chunk chunk) {
        if (!QuantumClient.isOnRenderThread()) {
            QuantumClient.invokeAndWait(() -> this.updateChunk(chunk));
            return;
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
                this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.BROKEN));
                this.client.connection.send(new C2SBlockBreakPacket(breaking));
            }
        }

        return breakResult;
    }

    /**
     * Stops the breaking of a block.
     *
     * @param breaking the position of the block being broken.
     * @param breaker  the player breaking the block.
     */
    @Override
    public void stopBreaking(@NotNull BlockVec breaking, @NotNull Player breaker) {
        // Check if the breaker is the local player
        if (breaker == this.client.player) {
            // Send a packet to stop the breaking process
            this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.STOP));
        }

        super.stopBreaking(breaking, breaker);
    }

    /**
     * Triggered when a chunk is updated.
     *
     * @param chunk the updated chunk.
     */
    @Override
    public void onChunkUpdated(@NotNull Chunk chunk) {
        // Check the current thread
        this.checkThread();

        super.onChunkUpdated(chunk);
    }

    /**
     * Plays a sound at a specific position.
     *
     * @param sound The sound event to be played.
     * @param x     The x position of the sound.
     * @param y     The y position of the sound.
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

    /**
     * Sets the block at the specified position to the given block properties.
     *
     * @param x The x-coordinate of the block position.
     * @param y The y-coordinate of the block position.
     * @param z The z-coordinate of the block position.
     * @param block The block properties to set.
     * @param flags Flags indicating how the block should be set.
     * @return True if the block was successfully set, false otherwise.
     */
    @Override
    public boolean set(int x, int y, int z, @NotNull BlockProperties block, int flags) {
        // Check if we're on the main thread, if not invokeAndWait the method on the main thread
        if (!QuantumClient.isOnRenderThread()) {
            return QuantumClient.invokeAndWait(() -> this.set(x, y, z, block, flags));
        }

        // Set the block and get the result
        boolean isBlockSet = super.set(x, y, z, block, flags);

        // Get the chunk containing the block
        BlockVec blockVec = new BlockVec(x, y, z);
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
            this.updateLightChunks(chunk);
        }

        // If the UPDATE flag is set and the chunk exists, update the chunk and its neighbors
        if ((flags & BlockFlags.UPDATE) != 0 && chunk != null && isBlockSet) {
            chunk.set(World.toLocalBlockVec(x, y, z, this.tmp), block);
            this.updateChunkAndNeighbours(chunk);

            // Update the blocks in each direction of the block
            for (CubicDirection direction : CubicDirection.values()) {
                BlockVec offset = blockVec.offset(direction);
                BlockProperties blockProperties = this.get(offset);
                blockProperties.update(this, offset);
            }
        }

        return isBlockSet;
    }

    @Override
    public void destroy(@NotNull BlockVec pos) {
        this.set(pos, BlockProperties.AIR);
    }

    @Override
    public void destroy(int x, int y, int z) {
        this.set(x, y, z, BlockProperties.AIR);
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
        return new ClientChunk[]{
                this.getChunk(chunk.getPos().getX() - 1, chunk.getPos().getZ() - 1),
                this.getChunk(chunk.getPos().getX(), chunk.getPos().getZ() - 1),
                this.getChunk(chunk.getPos().getX() + 1, chunk.getPos().getZ() - 1),
                this.getChunk(chunk.getPos().getX() - 1, chunk.getPos().getZ()),
                this.getChunk(chunk.getPos().getX() + 1, chunk.getPos().getZ()),
                this.getChunk(chunk.getPos().getX() - 1, chunk.getPos().getZ() + 1),
                this.getChunk(chunk.getPos().getX(), chunk.getPos().getZ() + 1),
                this.getChunk(chunk.getPos().getX() + 1, chunk.getPos().getZ() + 1)
        };
    }

    private void updateLightChunk(ClientChunk chunk) {
        if (client.world != null) {
            chunk.updateLight(client.world);
        }

        Vec3i offset = chunk.getOffset();
        for (int x = offset.x; x < offset.x + CHUNK_SIZE; x++) {
            for (int z = offset.x; z < offset.x + CHUNK_SIZE; z++) {
                setInitialSunlight(x, z);
            }
        }
    }

    public void setInitialSunlight(int startX, int startZ) {
        Queue<int[]> queue = new Queue<>();

        Chunk chunk = getChunkAt(startX, 0, startZ);
        if (chunk == null) return;

        // Start from the top of the world and move downward
        for (int y = WORLD_HEIGHT - 1; y >= 0; y--) {
            BlockVec localBlockVec = new BlockVec(startX, y, startZ);
            int lightReduction = chunk.get(toLocalBlockVec(startX, y, startZ)).getLightReduction();
            if (lightReduction < 15) {
                int intensity = 15 - lightReduction;
                setSunlight(localBlockVec.x(), localBlockVec.y(), localBlockVec.z(), intensity); // Assuming maximum sunlight intensity is 15
                queue.addLast(new int[]{localBlockVec.x(), localBlockVec.y(), localBlockVec.z(), intensity});
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
            BlockVec local = new BlockVec(current[0], current[1], current[2]);

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
        if (y >= 0 && y < WORLD_HEIGHT && reduction < 15 && getSunlight(x, y, z) < intensity) {
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

            // Skip if the current position is out of bounds
            if (y < 0 || y >= WORLD_HEIGHT) continue;

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
        BlockProperties blockProperties = chunkAt.get(toLocalBlockVec(x, y, z, this.tmp));
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
        if (data.pos.y + 1 < WORLD_HEIGHT)
            neighbors[2] = getBlockLightData(data.pos.x, data.pos.y + 1, data.pos.z);
        if (data.pos.y - 1 >= 0)
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
    public void updateLightSources(@NotNull Vec3i offset, ObjectMap<Vec3i, LightSource> lights) {
        // Call the superclass method to update light sources
        super.updateLightSources(offset, lights);

        // Update sunlight for each block in the chunk
        for (int x = offset.x; x < offset.x + CHUNK_SIZE; x++) {
            for (int z = offset.z; z < offset.z + CHUNK_SIZE; z++) {
                setInitialSunlight(x, z);
            }
        }

        // Process each light source and perform floodfill algorithm
        for (LightSource source : lights.values()) {
            int x = offset.x + source.x();
            int y = offset.y + source.y();
            int z = offset.z + source.z();
            int level = source.level();

            // Fill the region with light starting from the source position
            floodfill(x, y, z, level);
        }
    }

    private void updateSunlight(int blockX, int blockY, int blockZ, boolean isAdded) {
        if (blockY < 0 || blockY >= WORLD_HEIGHT) return;

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
                    int newLight = (y == WORLD_HEIGHT - 1) ? 15 : getSunlight(blockX, y + 1, blockZ) - reduction;
                    setSunlight(blockX, y, blockZ, newLight);
                    queue.addLast(new int[]{blockX, y, blockZ, newLight});
                } else {
                    break;
                }
            }
        }

        // Process the queue to update sunlight propagation
        while (!queue.isEmpty()) {
            // Remove the first element from the queue and get its position and light information
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
            if (y < WORLD_HEIGHT - 1 && reduction < 15) {
                updateNeighborSunlight(queue, x, y + 1, z, currentLight, reduction);
            }
        }
    }

    /**
     * Updates the sunlight of a neighboring block and adds it to the queue.
     *
     * @param queue        The queue to add the block to.
     * @param x            The x-coordinate of the block.
     * @param y            The y-coordinate of the block.
     * @param z            The z-coordinate of the block.
     * @param currentLight The current sunlight of the block.
     * @param reduction    The amount of sunlight to reduce.
     */
    private void updateNeighborSunlight(Queue<int[]> queue, int x, int y, int z, int currentLight, int reduction) {
        // Check if the block is within the world height limits
        if (y >= 0 && y < WORLD_HEIGHT) {
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

    private void sync(int x, int y, int z, BlockProperties block) {
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
        var chunk = QuantumClient.invokeAndWait(() -> this.chunks.get(pos));

        // If the chunk doesn't exist, set it to the new data
        if (chunk == null) {
            chunk = data;
        } else {
            // If the chunk already exists, log a warning and return
            World.LOGGER.warn("Duplicate chunk packet detected! Chunk {}", pos);
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

        // Calculate the distance between the chunk and the player
        if (new Vec2d(pos.getX(), pos.getZ()).dst(new Vec2d(player.getChunkVec().getX(), player.getChunkVec().getZ())) > ClientConfig.renderDistance) {
            // If the distance is greater than the render distance, send a skip chunk status packet and return
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SKIP));
            return;
        }

        // Set the final chunk to the chunk variable
        ClientChunk finalChunk = chunk;

        // Run the chunk loading logic on the client thread
        QuantumClient.invoke(() -> {
            synchronized (this) {
                // Add the chunk to the map of chunks
                this.chunks.put(pos, finalChunk);
                // Increment the total number of chunks
                this.totalChunks++;
                // Mark the chunk as ready
                finalChunk.ready();
            }
            // Send a success chunk status packet
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SUCCESS));
        });
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
            if (new Vec2d(chunkVec.getX(), chunkVec.getZ()).dst(player.getChunkVec().getX(), player.getChunkVec().getZ()) > ClientConfig.renderDistance) {
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
        int daytime = this.getDaytime();
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

    @Deprecated
    public RgbColor getSkyColor() {
        @Nullable TerrainRenderer worldRenderer = QuantumClient.get().worldRenderer;
        if (worldRenderer == null) return RgbColor.BLACK;
        return RgbColor.gdx(worldRenderer.getSkybox().topColor);
    }

    public int getDaytime() {
        return this.time % DAY_CYCLE;
    }

    static RgbColor mixColors(RgbColor color1, RgbColor color2, double percent) {
        percent = Mth.clamp(percent, 0.0, 1.0);
        double inversePercent = 1.0 - percent;
        int redPart = (int) (color1.getRed() * percent + color2.getRed() * inversePercent);
        int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inversePercent);
        int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inversePercent);
        int alphaPart = (int) (color1.getAlpha() * percent + color2.getAlpha() * inversePercent);
        return RgbColor.rgba(redPart, greenPart, bluePart, alphaPart);
    }

    @Override
    public void dispose() {
        this.checkThread();

        super.dispose();

        synchronized (this) {
            this.chunks.forEach((ChunkVec, clientChunk) -> clientChunk.dispose());
            this.chunks.clear();
        }
    }

    @Override
    public int getTotalChunks() {
        return this.totalChunks;
    }

    public void setDaytime(int daytime) {
        this.time = daytime;
    }

    public void addEntity(int id, EntityType<?> type, Vec3d position, MapType pipeline) {
        QuantumClient.LOGGER.debug("Adding entity with id {} of type {} at {}", new Object[]{id, type.getId(), position});

        Entity entity = type.create(this);
        entity.setId(id);
        entity.setPosition(position);
        entity.onPipeline(pipeline);
        this.entitiesById.put(id, entity);
    }

    @CanIgnoreReturnValue
    public Entity removeEntity(int id) {
        Entity remove = this.entitiesById.remove(id);
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
}
