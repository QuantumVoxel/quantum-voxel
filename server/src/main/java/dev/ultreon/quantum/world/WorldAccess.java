package dev.ultreon.quantum.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.Promise;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

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

    @Nullable ChunkAccess getChunkAt(@NotNull BlockVec pos);

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

    /**
     * Sets the block state at a specific column in a 2D grid.
     * This method is deprecated and may be removed in future versions.
     *
     * @param x The x-coordinate of the column.
     * @param z The z-coordinate of the column.
     * @param block The BlockState to set at the specified column.
     */
    @Deprecated
    void setColumn(int x, int z, BlockState block);

    /**
     * Sets the column of blocks in a 3D grid at the specified coordinates.
     * This method is deprecated and may be removed in future versions.
     *
     * @param x the x coordinate of the column
     * @param z the z coordinate of the column
     * @param maxY the maximum height (y coordinate) up to which blocks are set in the column
     * @param block the block state to set in the column
     */
    @Deprecated
    void setColumn(int x, int z, int maxY, BlockState block);

    /**
     * Asynchronously sets a cuboid region of blocks in the game world to a specified block state.
     *
     * @param x the X coordinate of the starting position.
     * @param y the Y coordinate of the starting position.
     * @param z the Z coordinate of the starting position.
     * @param width the width of the cuboid region.
     * @param height the height of the cuboid region.
     * @param depth the depth of the cuboid region.
     * @param block the BlockState to set for each block in the region.
     * @return a CompletionPromise that completes when the operation has finished.
     */
    Promise<Void> set(int x, int y, int z, int width, int height, int depth, BlockState block);

    /**
     * Retrieves a collection of all chunks that are currently loaded in memory.
     *
     * @return A collection containing the loaded chunks, represented by objects
     *         that extend the ChunkAccess interface.
     */
    Collection<? extends ChunkAccess> getLoadedChunks();

    /**
     * Sets the block entity at the specified position.
     *
     * @param pos The position vector of the block.
     * @param blockEntity The block entity to be set at the specified position.
     */
    void setBlockEntity(BlockVec pos, BlockEntity blockEntity);

    /**
     * Retrieves the BlockEntity located at the given BlockVec position.
     *
     * @param pos The BlockVec representing the position of the BlockEntity to retrieve.
     * @return The BlockEntity located at the specified position, or null if there is no BlockEntity at that position.
     */
    BlockEntity getBlockEntity(BlockVec pos);

    /**
     * Drops the specified item stack at the given position.
     *
     * @param itemStack the item stack to be dropped
     * @param position the position where the item stack should be dropped
     */
    void drop(ItemStack itemStack, Vec3d position);

    /**
     * Drops an item stack into the world at a specified position with a given velocity.
     *
     * @param itemStack the item stack to be dropped
     * @param position the position where the item stack will be dropped
     * @param velocity the initial velocity of the dropped item stack
     */
    void drop(ItemStack itemStack, Vec3d position, Vec3d velocity);

    /**
     * Finds and returns all entities within a certain distance from the specified entity.
     *
     * @param entity the reference entity from which the distance is calculated
     * @param distance the maximum distance within which other entities should be located
     * @return an iterable collection of entities found within the specified distance from the reference entity
     */
    Iterable<Entity> entitiesWithinDst(Entity entity, int distance);

    /**
     * Detects and returns entities that collide with the given item within the specified bounding box.
     *
     * @param droppedItem The entity representing the item that has been dropped.
     * @param ext The bounding box specifying the area to check for collisions with the dropped item.
     * @return An iterable collection of entities that collide with the dropped item within the bounding box.
     */
    Iterable<Entity> collideEntities(Entity droppedItem, BoundingBox ext);

    /**
     * Spawns a specified number of particles at a given position with a specified motion.
     *
     * @param particleType The type of particle to spawn.
     * @param position The initial position where the particles will spawn.
     * @param motion The velocity vector imparted to the spawned particles.
     * @param count The number of particles to spawn.
     */
    @ApiStatus.Experimental
    void spawnParticles(ParticleType particleType, Vec3d position, Vec3d motion, int count);

    /**
     * Destroys a block at the specified position, optionally considering the player who is breaking it.
     *
     * @param breaking The position vector of the block to destroy.
     * @param breaker The player breaking the block, or null if there is no player.
     * @return true if the block was successfully destroyed, false otherwise.
     */
    boolean destroyBlock(BlockVec breaking, @Nullable Player breaker);

    /**
     * Retrieves the block light level at a specific block coordinate in the world.
     *
     * @param x the x-coordinate of the block
     * @param y the y-coordinate of the block
     * @param z the z-coordinate of the block
     * @return the light level at the specified block coordinate
     */
    int getBlockLight(int x, int y, int z);

    /**
     * Sets the block light level at a specific block coordinate.
     *
     * @param x the x-coordinate of the block
     * @param y the y-coordinate of the block
     * @param z the z-coordinate of the block
     * @param intensity the light intensity to set at the specified block, typically ranging from 0 to 15
     */
    void setBlockLight(int x, int y, int z, int intensity);

    /**
     * Updates the light sources based on the given offset.
     *
     * @param offset the vector offset to apply to the light sources
     * @param lights the map of light sources to be updated, with Vec3i as keys representing positions and LightSource as values
     */
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

    @ApiStatus.Internal
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

    /**
     * Detects and returns a list of bounding boxes that collide with the given external bounding box.
     *
     * @param ext The external bounding box to check for collisions.
     * @param b A flag that may alter the collision detection behavior.
     * @return A list of bounding boxes that collide with the given external bounding box.
     */
    List<BoundingBox> collide(BoundingBox ext, boolean b);

    /**
     * Opens the specified container menu for user interaction.
     *
     * @param containerMenu the menu to be opened
     */
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

    /**
     * Retrieves the seed used by the world generator and other world-related RNG stuff.
     *
     * @return the current seed as a long value
     */
    long getSeed();

    /**
     * Sets the spawn point for an entity.
     *
     * @param spawnX The X-coordinate of the spawn point.
     * @param spawnZ The Z-coordinate of the spawn point.
     */
    void setSpawnPoint(int spawnX, int spawnZ);

    /**
     * Checks if the given chunk position is a spawn chunk.
     *
     * @param pos The ChunkVec representing the position of the chunk to check.
     * @return true if the chunk is a spawn chunk, false otherwise.
     */
    boolean isSpawnChunk(ChunkVec pos);

    /**
     * Retrieves the spawn point coordinates for the player.
     *
     * @return A BlockVec object representing the coordinates of the spawn point.
     */
    BlockVec getSpawnPoint();

    /**
     * Retrieves the number of chunks currently loaded in the game or application.
     *
     * @return The number of chunks that are loaded.
     */
    int getChunksLoaded();

    /**
     * Checks if the current object has been disposed of and is no longer usable.
     *
     * @return true if the object has been disposed of, false otherwise.
     */
    boolean isDisposed();

    /**
     * Called when a chunk has been updated.
     *
     * @param chunk the chunk that has been updated
     */
    void onChunkUpdated(Chunk chunk);

    /**
     * Plays a sound at the specified location.
     *
     * @param sound the sound event to be played
     * @param x the x-coordinate where the sound will be played
     * @param y the y-coordinate where the sound will be played
     * @param z the z-coordinate where the sound will be played
     */
    void playSound(SoundEvent sound, double x, double y, double z);

    /**
     * Closes the specified menu.
     *
     * @param containerMenu the menu to be closed
     */
    void closeMenu(ContainerMenu containerMenu);

    /**
     * Determines if the given bounding box intersects with any of the entities.
     *
     * @param boundingBox the bounding box to check for intersection with entities
     * @return true if the bounding box intersects with any entities, false otherwise
     */
    boolean intersectEntities(BoundingBox boundingBox);
}
