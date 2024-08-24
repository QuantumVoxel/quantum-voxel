package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface WorldReader {
    boolean isAlwaysLoaded(ChunkVec pos);

    @NotNull
    List<ChunkVec> getChunksAround(BlockVec pos);

    @NotNull
    BlockState get(BlockVec pos);

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
     * Casts a ray in the world and returns the result.
     *
     * @param ray      the ray to cast
     * @param distance the maximum distance that the ray can travel
     * @return the result
     */
    @NotNull
    default BlockHit rayCast(Ray ray, float distance) {
        BlockHit hitResult = new BlockHit(ray, distance);
        return WorldRayCaster.rayCast(hitResult, this);
    }

    @NotNull
    <T extends Entity> Iterable<Entity> getEntitiesByClass(Class<T> clazz);

    @NotNull
    UUID getUID();

    ChunkReader getChunkAt(int x, int y, int z);
}
