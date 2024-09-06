package dev.ultreon.quantum.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The {@code WorldAccess} interface provides a set of methods for interacting with the world,
 * including chunk management, block setting, entity handling, and light manipulation.
 * It extends the {@code Disposable} and {@code WorldReader} interfaces.
 */
public interface WorldAccess extends Disposable, WorldReader, BlockSetter {
    /**
     * Unloads the specified chunk from memory, freeing up resources.
     *
     * @param chunkVec the vector representing the chunk to be unloaded, must not be null
     * @return true if the chunk was successfully unloaded, false otherwise
     */
    boolean unloadChunk(@NotNull ChunkVec chunkVec);

    /**
     * Unloads a specified chunk from memory at the given position.
     *
     * @param chunk the chunk to be unloaded, which must not be null
     * @param pos the position of the chunk to be unloaded, which must not be null
     * @return true if the chunk was successfully unloaded, false otherwise
     */
    boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkVec pos);

    /**
     * Sets a block at the specified position with the given block state.
     *
     * @param pos the position of the block to set
     * @param block the BlockState to set at the specified position
     * @return true if the block was successfully set, false otherwise
     */
    boolean set(BlockVec pos, BlockState block);

    Array<Entity> getEntities();

    /**
     * Sets a block at the specified coordinates with the given block state and flags.
     *
     * @param x the x-coordinate of the block to set
     * @param y the y-coordinate of the block to set
     * @param z the z-coordinate of the block to set
     * @param block the BlockState to set at the specified coordinates
     * @param flags additional flags for setting the block
     * @return true if the block was successfully set, false otherwise
     */
    boolean set(int x, int y, int z, BlockState block, int flags);

    /**
     * Sets a block at the specified position with the given block state and flags.
     *
     * @param pos the position of the block to set
     * @param block the BlockState to set at the specified position
     * @param flags additional flags for setting the block
     * @return true if the block was successfully set, false otherwise
     */
    boolean set(BlockVec pos, BlockState block, int flags);

    ChunkAccess getChunkAt(@NotNull BlockVec pos);

    /**
     * Retrieves the chunk for a given position in the world.
     *
     * @param pos the position of the chunk to retrieve, encapsulated in a ChunkVec object
     * @return the ChunkAccess object representing the chunk at the specified position, or null if the chunk is not loaded
     */
    @Nullable
    ChunkAccess getChunk(ChunkVec pos);

    /**
     * Retrieves the chunk for the specified chunk coordinates in the world.
     *
     * @param x the chunk x-coordinate of the chunk
     * @param y the chunk y-coordinate of the chunk
     * @param z the chunk z-coordinate of the chunk
     * @return the ChunkAccess object representing the chunk at the specified coordinates, or null if the chunk is not loaded
     */
    @Nullable
    ChunkAccess getChunk(int x, int y, int z);

    /**
     * Retrieves the chunk at the specified block coordinates in the world.
     *
     * @param x the block x-coordinate of the chunk
     * @param y the block y-coordinate of the chunk
     * @param z the block z-coordinate of the chunk
     * @return the ChunkAccess object representing the chunk at the specified coordinates, or null if the chunk is not loaded
     */
    @Nullable
    ChunkAccess getChunkAt(int x, int y, int z);

    boolean isOutOfWorldBounds(BlockVec pos);

    boolean isOutOfWorldBounds(int x, int y, int z);

    default int getHeight(int x, int z) {
        return getHeight(x, z, HeightmapType.WORLD_SURFACE);
    }

    /**
     * Retrieves the height at the specified coordinates and heightmap type.
     *
     * @param x the x-coordinate
     * @param z the z-coordinate
     * @param type the type of heightmap to use
     * @return the height at the specified coordinates
     */
    int getHeight(int x, int z, HeightmapType type);

    /**
     * Retrieves the heightmap at the specified coordinates and for the given heightmap type.
     *
     * @param x the x-coordinate
     * @param z the z-coordinate
     * @param type the type of heightmap to retrieve
     * @return the Heightmap object representing the heightmap at the specified coordinates
     */
    Heightmap heightMapAt(int x, int z, HeightmapType type);

    @Deprecated
    void setColumn(int x, int z, BlockState block);

    void setColumn(int x, int z, int maxY, BlockState block);

    CompletableFuture<Void> set(int x, int y, int z, int width, int height, int depth, BlockState block);

    Collection<? extends ChunkAccess> getLoadedChunks();

    void setBlockEntity(BlockVec pos, BlockEntity blockEntity);

    BlockEntity getBlockEntity(BlockVec pos);

    void drop(ItemStack itemStack, Vec3d position);

    void drop(ItemStack itemStack, Vec3d position, Vec3d velocity);

    Iterable<Entity> entitiesWithinDst(Entity entity, int distance);

    Iterable<Entity> collideEntities(Entity droppedItem, BoundingBox ext);

    void spawnParticles(ParticleType particleType, Vec3d position, Vec3d motion, int count);

    boolean destroyBlock(BlockVec breaking, @Nullable Player breaker);

    int getBlockLight(int x, int y, int z);

    void setBlockLight(int x, int y, int z, int intensity);

    void updateLightSources(Vec3i offset, ObjectMap<Vec3i, LightSource> lights);

    /**
     * Performs a raycasting operation to find the first entity or block hit along a ray.
     * The raycasting checks for intersections with entities and blocks within the specified distance.
     *
     * @param ray the ray to cast
     * @param caster the entity casting the ray
     * @param distance the maximum distance the ray can travel
     * @param tmp a temporary vector for calculations
     * @return a Hit object representing the first entity or block hit, or a default hit object if no collision occurs
     */
    @NotNull
    default Hit rayCast(Ray ray, Entity caster, float distance, Vec3d tmp) {
        Entity closestToOrigin = null;
        double closestDst = Double.MAX_VALUE;
        for (Entity entity : getEntities()) {
            if (entity == caster) continue;
            if (Intersector.intersectRayBounds(ray, entity.getBoundingBox(), null)) {
                if (entity.getBoundingBox().getCenter(tmp).dst(ray.origin) < closestDst) {
                    closestToOrigin = entity;
                    closestDst = entity.getBoundingBox().getCenter(tmp).dst(ray.origin);
                }
            }
        }
        EntityHit entityHit = new EntityHit(ray, distance);
        entityHit.entity = closestToOrigin;
        entityHit.collide = true;

        if (closestToOrigin != null) {
            return entityHit;
        }

        BlockHit blockHit = WorldRayCaster.rayCast(new BlockHit(ray, entityHit.distance), this);
        if (blockHit.isCollide()) return blockHit;
        return entityHit;
    }

    void tick();

    /**
     * Despawns an entity based on the unique identifier provided.
     *
     * @param id the unique identifier of the entity to be despawned
     */
    void despawn(int id);

    /**
     * Retrieves an entity based on the provided identifier.
     *
     * @param id the unique identifier of the entity to retrieve
     * @return the entity associated with the given identifier
     */
    Entity getEntity(int id);

    List<BoundingBox> collide(BoundingBox ext, boolean b);

    @ApiStatus.Internal
    void openMenu(ContainerMenu containerMenu);

    /**
     * Checks if the current world instance is running on the client side.
     *
     * @return true if the world is running on the client side, false otherwise.
     */
    boolean isClientSide();

    boolean isServerSide();

    /**
     * Retrieves the biome at the specified block position.
     *
     * @param pos the position of the block as a BlockVec object
     * @return the RegistryKey representing the biome at the specified position
     */
    RegistryKey<Biome> getBiome(BlockVec pos);

    RegistryKey<DimensionInfo> getDimension();

    boolean isChunkInvalidated(Chunk chunk);

    void updateNeighbours(Chunk chunk);

    void updateChunkAndNeighbours(Chunk chunk);

    void updateChunk(@Nullable Chunk chunk);

    @ApiStatus.Obsolete
    <T extends Entity> T spawn(T entity);

    /**
     * Spawns the specified entity in the world with the given spawn data.
     *
     * @param entity the entity to be spawned
     * @param spawnData the data required for spawning the entity
     * @param <T> the type of the entity to be spawned
     * @return the spawned entity
     */
    <T extends Entity> T spawn(T entity, MapType spawnData);

    /**
     * Despawns the specified entity from the world.
     *
     * @param entity the entity to be despawned
     */
    void despawn(Entity entity);

    /**
     * Initiates the breaking process for a specified block by a player.
     *
     * @param breaking the position of the block that is being broken, encapsulated in a BlockVec object
     * @param breaker the player who is breaking the block
     */
    void startBreaking(BlockVec breaking, Player breaker);

    /**
     * Continues the breaking process of a specified block by a player with a given amount of damage.
     *
     * @param breaking the position of the block being broken, encapsulated in a BlockVec object
     * @param amount the amount of damage to apply to the breaking process
     * @param breaker the player who is breaking the block
     * @return an enumeration value of type BreakResult indicating the result of the breaking process
     */
    BreakResult continueBreaking(BlockVec breaking, float amount, Player breaker);

    /**
     * Stops the breaking process of the specified block by the indicated player.
     *
     * @param breaking the position of the block being broken, encapsulated in a BlockVec object
     * @param breaker the player who is breaking the block
     * @return true if the block breaking process was successfully stopped, false otherwise
     */
    boolean stopBreaking(BlockVec breaking, Player breaker);

    /**
     * Retrieves the progress of the block breaking process at the specified position.
     *
     * @param pos the position of the block as a BlockVec object
     * @return the break progress as a float value
     */
    float getBreakProgress(BlockVec pos);

    long getSeed();

    void setSpawnPoint(int spawnX, int spawnZ);

    boolean isSpawnChunk(ChunkVec pos);

    BlockVec getSpawnPoint();

    int getChunksLoaded();

    boolean isDisposed();

    void onChunkUpdated(Chunk chunk);

    void playSound(SoundEvent sound, double x, double y, double z);

    void closeMenu(ContainerMenu containerMenu);

    boolean intersectEntities(BoundingBox boundingBox);
}
