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

public interface WorldAccess extends Disposable, WorldReader {
    boolean unloadChunk(@NotNull ChunkVec chunkVec);

    boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkVec pos);

    boolean set(BlockVec pos, BlockState block);

    boolean set(int x, int y, int z, BlockState block);

    Array<Entity> getEntities();

    boolean set(int x, int y, int z, BlockState block, int flags);

    boolean set(BlockVec pos, BlockState block, int flags);

    default void destroy(@NotNull BlockVec pos) {
        destroy(pos.getIntX(), pos.getIntY(), pos.getIntZ());
    }

    default void destroy(int x, int y, int z) {
        set(x, y, z, BlockState.AIR, BlockFlags.UPDATE | BlockFlags.SYNC | BlockFlags.DESTROY);
    }

    ChunkAccess getChunkAt(@NotNull BlockVec pos);

    @Nullable
    ChunkAccess getChunk(ChunkVec pos);

    ChunkAccess getChunk(int x, int z);

    ChunkAccess getChunkAt(int x, int y, int z);

    boolean isOutOfWorldBounds(BlockVec pos);

    boolean isOutOfWorldBounds(int x, int y, int z);

    default int getHeight(int x, int z) {
        return getHeight(x, z, HeightmapType.WORLD_SURFACE);
    }

    int getHeight(int x, int z, HeightmapType type);

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
     * Casts a ray in the world and returns the result.
     * This uses the default of {@code 5} blocks.
     *
     * @param ray the ray to cast
     * @return the result
     */
    @NotNull
    default BlockHit rayCast(Ray ray) {
        return WorldRayCaster.rayCast(new BlockHit(ray), this);
    }

    void tick();

    void despawn(int id);

    Entity getEntity(int id);

    List<BoundingBox> collide(BoundingBox ext, boolean b);

    @ApiStatus.Internal
    void openMenu(ContainerMenu containerMenu);

    /**
     * @return true if the world is running on the client, false otherwise.
     */
    boolean isClientSide();

    boolean isServerSide();

    Biome getBiome(BlockVec pos);

    DimensionInfo getDimension();

    boolean isChunkInvalidated(Chunk chunk);

    @ApiStatus.Internal
    void updateNeighbours(Chunk chunk);

    @ApiStatus.Internal
    void updateChunkAndNeighbours(Chunk chunk);

    @ApiStatus.Internal
    void updateChunk(@Nullable Chunk chunk);

    @ApiStatus.Obsolete
    <T extends Entity> T spawn(T entity);

    <T extends Entity> T spawn(T entity, MapType spawnData);

    void despawn(Entity entity);

    void startBreaking(BlockVec breaking, Player breaker);

    BreakResult continueBreaking(BlockVec breaking, float amount, Player breaker);

    void stopBreaking(BlockVec breaking, Player breaker);

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
