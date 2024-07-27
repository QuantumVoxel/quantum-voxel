package dev.ultreon.quantum.world;

import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.util.BlockHitResult;
import dev.ultreon.quantum.util.EntityHitResult;
import dev.ultreon.quantum.util.Ray;
import dev.ultreon.quantum.util.WorldRayCaster;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface WorldReader {
    boolean isAlwaysLoaded(ChunkPos pos);

    @NotNull
    List<ChunkPos> getChunksAround(Vec3d pos);

    @NotNull
    BlockProperties get(BlockPos pos);

    @NotNull
    BlockProperties get(int x, int y, int z);

    @NotNull
    EntityHitResult rayCastEntity(Ray ray);

    @NotNull
    EntityHitResult rayCastEntity(Ray ray, float distance);

    @NotNull
    EntityHitResult rayCastEntity(Ray ray, float distance, Predicate<Entity> filter);

    @NotNull
    EntityHitResult rayCastEntity(Ray ray, float distance, EntityType<?> type);

    @NotNull
    EntityHitResult rayCastEntity(Ray ray, float distance, Class<? extends Entity> type);

    /**
     * Casts a ray in the world and returns the result.
     *
     * @param ray      the ray to cast
     * @param distance the maximum distance that the ray can travel
     * @return the result
     */
    @NotNull
    default BlockHitResult rayCast(Ray ray, float distance) {
        BlockHitResult hitResult = new BlockHitResult(ray, distance);
        return WorldRayCaster.rayCast(hitResult, this);
    }

    @NotNull
    <T extends Entity> Iterable<Entity> getEntitiesByClass(Class<T> clazz);

    @NotNull
    UUID getUID();

    ChunkReader getChunkAt(int x, int y, int z);
}
