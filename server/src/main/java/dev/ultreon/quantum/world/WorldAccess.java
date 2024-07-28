package dev.ultreon.quantum.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.util.BlockHitResult;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.util.Ray;
import dev.ultreon.quantum.util.WorldRayCaster;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface WorldAccess extends Disposable, WorldReader {
    boolean unloadChunk(@NotNull ChunkPos chunkPos);

    boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkPos pos);

    boolean set(BlockPos pos, BlockProperties block);

    boolean set(int x, int y, int z, BlockProperties block);

    Array<Entity> getEntities();

    boolean set(int x, int y, int z, BlockProperties block, int flags);

    boolean set(BlockPos pos, BlockProperties block, int flags);

    default void destroy(@NotNull BlockPos pos) {
        destroy(pos.x(), pos.y(), pos.z());
    }

    default void destroy(int x, int y, int z) {
        set(x, y, z, BlockProperties.AIR, BlockFlags.UPDATE | BlockFlags.SYNC | BlockFlags.DESTROY);
    }

    ChunkAccess getChunkAt(@NotNull BlockPos pos);

    @Nullable
    ChunkAccess getChunk(ChunkPos pos);

    ChunkAccess getChunk(int x, int z);

    ChunkAccess getChunkAt(int x, int y, int z);

    boolean isOutOfWorldBounds(BlockPos pos);

    boolean isOutOfWorldBounds(int x, int y, int z);

    int getHighest(int x, int z);

    void setColumn(int x, int z, BlockProperties block);

    void setColumn(int x, int z, int maxY, BlockProperties block);

    CompletableFuture<Void> set(int x, int y, int z, int width, int height, int depth, BlockProperties block);

    Collection<? extends ChunkAccess> getLoadedChunks();

    void setBlockEntity(BlockPos pos, BlockEntity blockEntity);

    BlockEntity getBlockEntity(BlockPos pos);

    void drop(ItemStack itemStack, Vec3d position);

    void drop(ItemStack itemStack, Vec3d position, Vec3d velocity);

    Iterable<Entity> entitiesWithinDst(Entity entity, int distance);

    Iterable<Entity> collideEntities(Entity droppedItem, BoundingBox ext);

    void spawnParticles(ParticleType particleType, Vec3d position, Vec3d motion, int count);

    boolean destroyBlock(BlockPos breaking, @Nullable Player breaker);

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
    default BlockHitResult rayCast(Ray ray) {
        return WorldRayCaster.rayCast(new BlockHitResult(ray), this);
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

    Biome getBiome(BlockPos pos);

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

    void startBreaking(BlockPos breaking, Player breaker);

    BreakResult continueBreaking(BlockPos breaking, float amount, Player breaker);

    void stopBreaking(BlockPos breaking, Player breaker);

    float getBreakProgress(BlockPos pos);

    long getSeed();

    void setSpawnPoint(int spawnX, int spawnZ);

    boolean isSpawnChunk(ChunkPos pos);

    BlockPos getSpawnPoint();

    int getChunksLoaded();

    boolean isDisposed();

    void onChunkUpdated(Chunk chunk);

    void playSound(SoundEvent sound, double x, double y, double z);

    void closeMenu(ContainerMenu containerMenu);

    boolean intersectEntities(BoundingBox boundingBox);
}
