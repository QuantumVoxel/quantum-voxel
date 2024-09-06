package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.util.EntityHit;
import dev.ultreon.quantum.util.Ray;
import dev.ultreon.quantum.util.WorldRayCaster;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * The WorldReader interface provides methods to interact with and query the state of the world.
 * It includes functionalities for checking chunk states, retrieving block and entity information,
 * and performing ray casting operations.
 */
public interface WorldReader {
    /**
     * Checks if the chunk specified by the ChunkVec position is always loaded in the world.
     *
     * @param pos the ChunkVec position representing the chunk
     * @return true if the chunk is always loaded; false otherwise
     */
    boolean isAlwaysLoaded(ChunkVec pos);

    /**
     * Retrieves the BlockState at the specified BlockVec position within the world.
     *
     * @param pos the BlockVec position of the block
     * @return the BlockState at the given BlockVec position
     */
    @NotNull
    BlockState get(BlockVec pos);

    /**
     * Retrieves the BlockState at the specified coordinates within the world.
     *
     * @param x the x-coordinate of the block
     * @param y the y-coordinate of the block
     * @param z the z-coordinate of the block
     * @return the BlockState at the given coordinates
     */
    @NotNull
    BlockState get(int x, int y, int z);

    @NotNull
    EntityHit rayCastEntity(Ray ray);

    @NotNull
    EntityHit rayCastEntity(Ray ray, float distance);

    @NotNull
    EntityHit rayCastEntity(Ray ray, float distance, Predicate<Entity> filter);

    @NotNull
    EntityHit rayCastEntity(Ray ray, float distance, EntityType<?> type);

    @NotNull
    EntityHit rayCastEntity(Ray ray, float distance, Class<? extends Entity> type);

    /**
     * Casts a ray into the world to find the first block it intersects within a specified distance.
     *
     * @param ray The ray to be cast.
     * @param distance The maximum distance the ray will travel.
     * @return A BlockHit object containing information about the intersection, if any.
     */
    @NotNull
    default BlockHit rayCast(Ray ray, float distance) {
        BlockHit hitResult = new BlockHit(ray, distance);
        return WorldRayCaster.rayCast(hitResult, this);
    }

    @NotNull
    <T extends Entity> Iterable<Entity> getEntitiesByClass(Class<T> clazz);

    /**
     * Retrieves the unique identifier (UUID) of the world.
     *
     * @return the UUID representing the world.
     */
    @NotNull
    UUID getUID();

    /**
     * Retrieves the ChunkReader at the specified coordinates.
     *
     * @param x the x-coordinate of the chunk
     * @param y the y-coordinate of the chunk
     * @param z the z-coordinate of the chunk
     * @return the ChunkReader at the specified coordinates
     */
    ChunkReader getChunkAt(int x, int y, int z);
}
