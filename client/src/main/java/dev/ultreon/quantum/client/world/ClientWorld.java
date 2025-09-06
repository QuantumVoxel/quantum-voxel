package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.*;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.debug.BoxGizmo;
import dev.ultreon.quantum.client.debug.Gizmo;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.player.RemotePlayer;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.render.world.WorldRenderer;
import dev.ultreon.quantum.client.util.Renderable;
import dev.ultreon.quantum.client.util.Rot;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.network.packets.c2s.C2SBlockBreakPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SBlockBreakingPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SPlaceBlockPacket;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.ChunkVecSpace;
import dev.ultreon.quantum.ubo.types.MapType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import static com.badlogic.gdx.math.MathUtils.lerp;
import static java.lang.Math.max;

@SuppressWarnings("GDXJavaUnsafeIterator")
public final class ClientWorld extends World implements Disposable, Renderable, ClientWorldAccess {
    public static final int DAY_CYCLE = 24000;

    public static final AtomicReference<RgbColor> FOG_COLOR = new AtomicReference<>(RgbColor.rgb(0x7fb0fe));
    public static final AtomicReference<Vec2> ATLAS_SIZE = new AtomicReference<>(new Vec2(2048, 2048));
    public static double FOG_DENSITY = 0.001;
    public static double FOG_START = 0.0;
    public static double FOG_END = 1.0;

    public static final Color DAY_TOP_COLOR = new Color(0x7fb0feff);
    public static final Color DAY_BOTTOM_COLOR = new Color(0xc1d3f1ff);
    public static final Color NIGHT_TOP_COLOR = new Color(0x01010bff);
    public static final Color NIGHT_BOTTOM_COLOR = new Color(0x0a0c16ff);
    public static final Color SUN_RISE_COLOR = new Color(0xff6000ff);
    public static final Color VOID_COLOR = new Color(0x0a0a0aff);

    public static final AtomicReference<Vec2> ATLAS_OFFSET = new AtomicReference<>(new Vec2(1 + 1 - (ATLAS_SIZE.get().x / (ATLAS_SIZE.get().x)), ATLAS_SIZE.get().y));

    public static Rot SKYBOX_ROTATION = Rot.deg(-60);
    public static int VOID_Y_START = 20;
    public static int VOID_Y_END = 0;
    public final QuantumClient client;
    private final RegistryKey<DimensionInfo> dimension;
    private final ClientChunkManager chunkManager = new ClientChunkManager(this);
    private int chunkRefresh;
    private ChunkVec oldChunkVec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
    private long time = 0;
    private int totalChunks;
    private final IVec3 tmp = new IVec3();
    private final Queue<LightData> panelQueue = new Queue<>();
    private final ObjectIntMap<LightData> panelMap = new ObjectIntMap<>();

    private final ObjectMap<String, Array<Gizmo>> gizmos = new ObjectMap<>();
    private final ObjectSet<String> enabledCategories = new ObjectSet<>();
    private final ClientEntityManager entityManager = new ClientEntityManager(this);

    /**
     * Constructor for creating an instance of ClientWorld.
     *
     * @param client    an instance of QuantumClient which interacts with the client-side quantum framework.
     * @param dimension a RegistryKey of DimensionInfo representing the dimension of the world.
     */
    public ClientWorld(QuantumClient client, RegistryKey<DimensionInfo> dimension) {
        super();
        this.client = client;
        this.dimension = dimension;

        this.add("Chunk Manager", chunkManager);
        this.add("Entity Manager", entityManager);
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
        return ClientConfiguration.renderDistance.getValue() / CS;
    }

    /**
     * Unloads a chunk from the world.
     *
     * @param chunk The chunk to unload.
     * @param pos   The position of the chunk.
     * @return True if the chunk was successfully unloaded, false otherwise.
     */
    @Override
    public boolean unloadChunk(Chunk chunk, ChunkVec pos) {
        if (!(chunk instanceof ClientChunk))
            throw new IllegalArgumentException("Chunk must be a ClientChunk");
        ClientChunk clientChunk = (ClientChunk) chunk;

        // Check if the chunk should stay loaded
        if (shouldStayLoaded(pos)) {
            // If it should, return false
            return false;
        }

        // Try to remove the chunk from the chunk map
        synchronized (this.chunkManager){
            ClientChunk removedChunk = this.chunkManager.remove(pos.x, pos.y, pos.z);
            boolean removed = removedChunk != null;
            if (removed) {
                // If the chunk was removed, decrement the total number of chunks
                TerrainRenderer worldRenderer = this.client.worldRenderer;
                if (worldRenderer != null)
                    QuantumClient.invoke(() -> worldRenderer.unload(clientChunk));

                if (removedChunk != chunk)
                    LOGGER.warn("Removed chunk mismatch: {} != {}", removedChunk, chunk);

                this.totalChunks--;
                this.chunkManager.remove((GameObject) removedChunk);
            }

            // Return true if the chunk was removed, false otherwise
            return removed;
        }
    }

    @Override
    protected void checkThread() {

    }

    @Override
    public @Nullable ClientChunk getChunk(ChunkVec pos) {
        return this.chunkManager.get(pos.x, pos.y, pos.z);
    }

    static long chunkKey(int x, int y, int z) {
        return (((long) x) & 0xFFFFF) | ((((long) y) & 0xFFFFF) << 20) | ((((long) z) & 0xFFFFF) << 40);
    }

    @Override
    public @Nullable ClientChunk getChunk(int x, int y, int z) {
        return chunkManager.get(x, y, z);
    }

    @Override
    public @Nullable ClientChunk getChunkAt(BlockVec pos) {
        return (ClientChunk) super.getChunkAt(pos);
    }

    @Override
    @Nullable
    public ClientChunk getChunkAt(int x, int y, int z) {
        return (ClientChunk) super.getChunkAt(x, y, z);
    }

    @Override
    public Collection<ClientChunk> getLoadedChunks() {
        return this.chunkManager.getAllChunks();
    }

    @Override
    public boolean isChunkInvalidated(Chunk chunk) {
        return super.isChunkInvalidated(chunk);
    }

    @Override
    public void updateChunkAndNeighbours(Chunk chunk) {
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

        if (!(chunk instanceof ClientChunk))
            throw new IllegalArgumentException("Chunk must be a ClientChunk but was " + chunk.getClass().getSimpleName());
        ClientChunk clientChunk = (ClientChunk) chunk;

        if (this.client.worldRenderer instanceof WorldRenderer) {
            WorldRenderer worldRenderer = this.client.worldRenderer;
            worldRenderer.rebuild(clientChunk);
        }

        super.updateChunk(chunk);
    }

    @Override
    public void startBreaking(BlockVec breaking, Player breaker) {
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
    public BreakResult continueBreaking(BlockVec breaking, float amount, Player breaker) {
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
    public boolean stopBreaking(BlockVec breaking, Player breaker) {
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
    public void onChunkUpdated(Chunk chunk) {
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
    public void playSound(SoundEvent sound, double x, double y, double z) {
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
    public boolean set(int x, int y, int z, BlockState block, int flags) {
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
    public boolean set(BlockVec pos, BlockState block, int flags) {
        return set(pos.x, pos.y, pos.z, block, flags);
    }

    @Override
    public int getSunlight(int x, int y, int z) {
        ClientChunk chunk = getChunkAt(x, y, z);
        if (chunk != null) {
            return chunk.getSunlight(toLocalBlockVec(x, y, z));
        }

        return 0;
    }

    public void resetQueue() {
        panelQueue.clear();
        panelMap.clear();
    }

    public int getBlockLight(IVec3 pos) {
        return getBlockLight(pos.x, pos.y, pos.z);
    }

    public void setBlockLight(LightData data, int level) {
        this.setBlockLight(data.pos, level);
    }

    public void setBlockLight(IVec3 pos, int level) {
        this.setBlockLight(pos.x, pos.y, pos.z, level);
    }

    @Override
    public int getBlockLight(int x, int y, int z) {
        ClientChunk chunk = getChunkAt(x, y, z);
        if (chunk == null) return 0;
        return (byte) chunk.getBlockLight(toLocalBlockVec(x, y, z, this.tmp));
    }

    @Override
    public void setBlockLight(int x, int y, int z, int light) {
        ClientChunk chunk = this.getChunkAt(x, y, z);
        if (chunk != null) {
            chunk.setBlockLight(toLocalBlockVec(x, y, z, this.tmp), light);
        }
    }

    @Override
    public boolean isLoaded(Chunk chunk) {
        if (chunk instanceof ClientChunk) {
            ClientChunk clientChunk = (ClientChunk) chunk;
            return this.chunkManager.contains(clientChunk);
        }

        return false;
    }

    @Override
    @ApiStatus.Experimental
    public void spawnParticles(ParticleType particleType, DVec3 position, DVec3 motion, int count) {
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
        chunk = this.chunkManager.get(pos.x, pos.y, pos.z);

        // If the chunk doesn't exist, set it to the new data
        if (chunk == null) {
            chunk = data;
        } else {
            // If the chunk already exists, log a warning and return
            if (DebugFlags.CHUNK_LOADER_DEBUG.isEnabled()) World.LOGGER.warn("Chunk already exists: {}", pos);
            TerrainRenderer worldRenderer = this.client.worldRenderer;
            if (worldRenderer != null) {
                ClientChunk finalChunk = chunk;
                QuantumClient.invoke(() -> worldRenderer.unload(finalChunk));
            }
            return;
        }

        // Get the local player
        LocalPlayer player = this.client.player;

        // If the player is null, send a failed chunk status packet and return
        if (player == null) {
            return;
        }

        QuantumClient.invoke(() -> {
            BoxGizmo gizmo = new BoxGizmo(data, "chunk_bounds", "chunk");
            gizmo.position.set(data.getOffset().vec().d().add(8.0, 8.0, 8.0));
            gizmo.size.set(CS, CS, CS);
            gizmo.color.set(1.0F, 0.0F, 0.0F, 1.0F);
            gizmo.outline = true;
            this.addGizmo(gizmo);
        });

        // Calculate the distance between the chunk and the player
        synchronized (this.chunkManager) {
            // Add the chunk to the map of chunks
            this.chunkManager.add(chunk);
            this.chunkManager.add("Chunk " + pos, chunk);
            // Increment the total number of chunks
            this.totalChunks++;
            // Mark the chunk as ready
            chunk.ready();
        }
    }

    public @Nullable Gizmo getEntityGizmo(Entity entity) {
        RenderEntity entity1 = entityManager.getEntity(entity.getId());
        if (entity1 == null) return null;
        return entity1.boundsGizmo;
    }

    public @Nullable RenderEntity getRenderEntity(Entity entity) {
        return entityManager.getEntity(entity.getId());
    }

    public void removeEntity(Entity entity) {
        entityManager.removeEntity(entity);
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
        for (Iterator<ClientChunk> iterator = this.chunkManager.iterator(); iterator.hasNext(); ) {
            ClientChunk clientChunk = iterator.next();
            ChunkVec chunkVec = clientChunk.vec;

            // Check if the distance between the chunk and the player's position is greater than the render distance
            if (chunkVec.dst(player.getChunkVec()) > (double) ClientConfiguration.renderDistance.getValue() / CS) {
                CommonConstants.LOGGER.trace("Chunk {} is out of render distance, removing", chunkVec);

                // Remove the chunk from the map and dispose it
                iterator.remove();
                clientChunk.dispose();

                // Update the neighbours of the chunk
                this.updateNeighbours(clientChunk);
            }
        }
    }

    @Override
    public Array<Entity> getAllEntities() {
        return this.entitiesById.values().toArray();
    }

    @Override
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
        return this.chunkManager.contains(chunkVec.x, chunkVec.y, chunkVec.z);
    }

    @Override
    public void onBlockSet(BlockVec pos, BlockState block) {
        @Nullable ClientChunk chunkAt = this.getChunkAt(pos);
        if (chunkAt == null)
            return;
        chunkAt.set(pos.chunkLocal(), block);

        chunkAt.dirty = true;
        for (@Nullable Chunk chunk : chunkAt.neighbors) {
            if (chunk != null)
                chunk.dirty = true;
        }
    }

    @Override
    public long getDaytime() {
        return this.time % DAY_CYCLE;
    }

    static Color mixColors(Color color1, Color color2, Color output, double percent) {
        percent = Mth.clamp(percent, 0.0, 1.0);
        double inversePercent = 1.0 - percent;
        float redPart = (float) (color1.r * percent + color2.r * inversePercent);
        float greenPart = (float) (color1.g * percent + color2.g * inversePercent);
        float bluePart = (float) (color1.b * percent + color2.b * inversePercent);
        float alphaPart = (float) (color1.a * percent + color2.a * inversePercent);
        return output.set(redPart, greenPart, bluePart, alphaPart);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public int getTotalChunks() {
        return this.totalChunks;
    }

    @Override
    public void setDaytime(long daytime) {
        this.time = daytime;
    }

    @Override
    public void addEntity(int id, EntityType<?> type, DVec3 position, MapType pipeline) {
        QuantumClient.LOGGER.debug("Adding entity with id {} of type {} at {}", id, type.getId(), position);

        Entity entity = type.create(this);
        entity.setId(id);
        entity.setPosition(position);
        entity.onPipeline(pipeline);
        BoxGizmo boxGizmo = new BoxGizmo(entity, "entity_bounds", "entity-bounds");
        boxGizmo.position.set(position);
        boxGizmo.size.set(entity.getSize().width(), entity.getSize().height(), entity.getSize().width());
        boxGizmo.outline = true;
        RenderEntity renderEntity = this.entityManager.addEntity(entity);
        renderEntity.boundsGizmo = boxGizmo;
        this.addGizmo(boxGizmo);
        this.entitiesById.put(id, entity);
    }

    @Override
    public Entity removeEntity(int id) {
        Entity remove = this.entitiesById.remove(id);
        Gizmo boundsGizmo = this.entityManager.removeEntity(remove).boundsGizmo;
        if (boundsGizmo != null) this.removeGizmo(boundsGizmo);
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
    @Override
    public void onPlayerAttack(int playerId, int entityId) {
        // Get the player entity and the target entity based on their IDs
        Entity player = this.entitiesById.get(playerId);
        Entity entity = this.entitiesById.get(entityId);

        // If the target entity exists and the player is a remote player, trigger the attack
        if (entity != null && player instanceof RemotePlayer) {
            RemotePlayer remotePlayer = (RemotePlayer) player;
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

    public void addEntity(Entity entity) {
        if (entity.getId() == -1) throw new IllegalArgumentException("Entity ID not set");
        this.entitiesById.put(entity.getId(), entity);
    }

    @Override
    public void render(RenderBufferSource source) {

    }
}
