package dev.ultreon.quantum.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.entity.DroppedItem;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.events.BlockEvents;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.util.Utils;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.ServerWorld.Region;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.ubo.types.LongType;
import dev.ultreon.ubo.types.MapType;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Base class for client/server worlds.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @see Chunk
 * @see Region
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
@ApiStatus.NonExtendable
@ParametersAreNonnullByDefault
public abstract class World implements Disposable, WorldAccess {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_HEIGHT = 256;
    public static final int WORLD_HEIGHT = 256;
    public static final int WORLD_DEPTH = 0;
    public static final int REGION_SIZE = 32;
    public static final NamespaceID OVERWORLD = new NamespaceID("overworld");
    public static final int SEA_LEVEL = 64;
    public static final int REGION_DATA_VERSION = 0;

    protected static final Logger LOGGER = LoggerFactory.getLogger(World.class);

    protected final Vec3i spawnPoint = new Vec3i();
    protected final long seed;
    private int renderedChunks;

    protected final IntMap<Entity> entitiesById = new IntMap<>();
    private int curId;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<ChunkVec> alwaysLoaded = new ArrayList<>();
    final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(), 1), r -> {
        Thread thread = new Thread(r);
        thread.setName("ChunkBuilder");
        thread.setDaemon(true);
        return thread;
    });

    boolean disposed;
    private final Set<ChunkVec> invalidatedChunks = new LinkedHashSet<>();
    private final List<ContainerMenu> menus = new ArrayList<>();
    private final DimensionInfo info = DimensionInfo.OVERWORLD; // TODO WIP
    protected UUID uid = Utils.ZEROED_UUID;
    protected int spawnX;
    protected int spawnZ;

    protected World() {
        // Shh, the original seed was 512.S
        this(512/*new Random().nextLong()*/);
    }

    protected World(long seed) {
        this.seed = seed;
    }

    public World(@Nullable LongType seed) {
        this(seed == null ? new Random().nextLong() : seed.getValue());
    }

    static List<ChunkVec> getChunksAround(World world, Vec3d pos) {
        int startX = (int) (pos.x - world.getRenderDistance() * World.CHUNK_SIZE);
        int startZ = (int) (pos.z - world.getRenderDistance() * World.CHUNK_SIZE);
        int endX = (int) (pos.x + world.getRenderDistance() * World.CHUNK_SIZE);
        int endZ = (int) (pos.z + world.getRenderDistance() * World.CHUNK_SIZE);

        List<ChunkVec> toCreate = new ArrayList<>();
        for (int x = startX; x <= endX; x += World.CHUNK_SIZE) {
            for (int z = startZ; z <= endZ; z += World.CHUNK_SIZE) {
                ChunkVec chunkVec = Utils.ChunkVecFromBlockCoords(new Vec3d(x, 0, z));
                toCreate.add(chunkVec);
                if (x >= pos.x - World.CHUNK_SIZE
                    && x <= pos.x + World.CHUNK_SIZE
                    && z >= pos.z - World.CHUNK_SIZE
                    && z <= pos.z + World.CHUNK_SIZE) {
                    for (int y = -World.CHUNK_HEIGHT; y >= pos.y - World.CHUNK_HEIGHT * 2; y -= World.CHUNK_HEIGHT) {
                        chunkVec = Utils.ChunkVecFromBlockCoords(new Vec3d(x, y, z));
                        toCreate.add(chunkVec);
                    }
                }
            }
        }

        return toCreate;
    }

    public static ChunkVec blockToChunkVec(Vector3 pos) {
        return new ChunkVec(Math.floorDiv((int) pos.x, World.CHUNK_SIZE), Math.floorDiv((int) pos.z, World.CHUNK_SIZE));
    }

    public static ChunkVec blockToChunkVec(Vec3d pos) {
        return new ChunkVec(Math.floorDiv((int) pos.x, World.CHUNK_SIZE), Math.floorDiv((int) pos.z, World.CHUNK_SIZE));
    }

    public static ChunkVec blockToChunkVec(Vec3i pos) {
        return new ChunkVec(Math.floorDiv(pos.x, World.CHUNK_SIZE), Math.floorDiv(pos.z, World.CHUNK_SIZE));
    }

    public static ChunkVec toChunkVec(BlockVec pos) {
        return new ChunkVec(Math.floorDiv(pos.x(), World.CHUNK_SIZE), Math.floorDiv(pos.z(), World.CHUNK_SIZE));
    }

    public static ChunkVec toChunkVec(int x, int y, int z) {
        return new ChunkVec(Math.floorDiv(x, World.CHUNK_SIZE), Math.floorDiv(z, World.CHUNK_SIZE));
    }

    @Override
    public @NotNull List<ChunkVec> getChunksAround(Vec3d pos) {
        int renderDistance = this.getRenderDistance();
        int startX = (int) (pos.x - renderDistance * World.CHUNK_SIZE);
        int startZ = (int) (pos.z - renderDistance * World.CHUNK_SIZE);
        int endX = (int) (pos.x + renderDistance * World.CHUNK_SIZE);
        int endZ = (int) (pos.z + renderDistance * World.CHUNK_SIZE);

        List<ChunkVec> toCreate = new ArrayList<>();
        for (int x = startX; x <= endX; x += World.CHUNK_SIZE) {
            for (int z = startZ; z <= endZ; z += World.CHUNK_SIZE) {
                ChunkVec chunkVec = Utils.ChunkVecFromBlockCoords(new Vec3d(x, 0, z));
                toCreate.add(chunkVec);
            }
        }

        return toCreate;
    }

    protected abstract int getRenderDistance();

    protected boolean shouldStayLoaded(ChunkVec pos) {
		return this.isSpawnChunk(pos) || this.isAlwaysLoaded(pos);
    }

    @Override
    public boolean isAlwaysLoaded(ChunkVec pos) {
        return this.alwaysLoaded.contains(pos);
    }

    @Deprecated
    private CompletableFuture<Boolean> unloadChunkAsync(ChunkVec chunkVec) {
        return this.unloadChunkAsync(Objects.requireNonNull(this.getChunk(chunkVec), "Chunk not loaded: " + chunkVec));
    }

    @Deprecated
    @CanIgnoreReturnValue
    private CompletableFuture<Boolean> unloadChunkAsync(@NotNull Chunk chunk) {
        synchronized (chunk.lock) {
            return CompletableFuture.supplyAsync(() -> this.unloadChunk(chunk, chunk.getPos()), this.executor).exceptionally(throwable -> {
                World.fail(throwable, "Failed to unload chunk:");
                return false;
            });
        }
    }

    private static void fail(Throwable throwable, String msg) {
        if (throwable instanceof CompletionException e && throwable.getCause() instanceof Error) {
            Error error = (Error) e.getCause();
            QuantumServer.get().crash(throwable);
        }
        if (throwable instanceof Error error) {
            QuantumServer.get().crash(throwable);
        }

        World.LOGGER.error(msg, throwable);
    }

    @Override
    public boolean unloadChunk(@NotNull ChunkVec chunkVec) {
        this.checkThread();

        Chunk chunk = this.getChunk(chunkVec);
        if (chunk == null) return true;
        return this.unloadChunk(chunk, chunkVec);
    }

    @Override
    @CanIgnoreReturnValue
    public abstract boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkVec pos);

    @Override
    public @NotNull EntityHitResult rayCastEntity(Ray ray) {
        return rayCastEntity(ray, 5);
    }

    @Override
    public @NotNull EntityHitResult rayCastEntity(Ray ray, float distance) {
        EntityHitResult result = new EntityHitResult(ray, distance);
        Stream<Entity> entitiesWithin = getEntitiesWithin(new BoundingBox(ray.origin, ray.origin.add(ray.direction.cpy().mul(distance))));
        entitiesWithin.forEach(entity -> {
            double curDistance = ray.origin.dst(entity.getPosition());
            Vec3d intersection = new Vec3d();
            if (Intersector.intersectRayBounds(ray, entity.getBoundingBox(), intersection) && (result.getEntity() == null || curDistance < result.getDistance() && curDistance < result.getDistanceMax())) {
                result.entity = entity;
                result.distance = curDistance;
                result.position = intersection;
                result.collide = true;
            }
        });

        return result;
    }

    @Override
    public @NotNull EntityHitResult rayCastEntity(Ray ray, float distance, Predicate<Entity> filter) {
        EntityHitResult result = new EntityHitResult(ray, distance);
        Stream<Entity> entitiesWithin = getEntitiesWithin(new BoundingBox(ray.origin, ray.origin.add(ray.direction.cpy().mul(distance))));
        entitiesWithin.forEach(entity -> {
            if (filter.test(entity)) {
                double curDistance = ray.origin.dst(entity.getPosition());
                if (result.getEntity() == null || curDistance < result.getDistance() && curDistance < result.getDistanceMax()) {
                    result.entity = entity;
                    result.distance = curDistance;
                }
            }
        });

        if (result.getEntity() != null) {
            result.collide = true;
        }

        return result;
    }

    @Override
    public @NotNull EntityHitResult rayCastEntity(Ray ray, float distance, EntityType<?> type) {
        return rayCastEntity(ray, distance, e -> e.getType() == type);
    }

    @Override
    public @NotNull EntityHitResult rayCastEntity(Ray ray, float distance, Class<? extends Entity> type) {
        return rayCastEntity(ray, distance, type::isInstance);
    }

    private Stream<Entity> getEntitiesWithin(BoundingBox boundingBox) {
        return Arrays.stream(this.entitiesById.values().toArray().toArray(Entity.class)).filter(entity -> boundingBox.intersects(entity.getBoundingBox()));
    }

    /**
     * Sets the block at the specified coordinates, with the given block type.
     *
     * @param blockVec the position
     * @param state    the block state to set
     * @return true if the block was successfully set, false otherwise
     */
    @Override
    public boolean set(BlockVec blockVec, BlockProperties state) {
        return set(blockVec, state, BlockFlags.UPDATE | BlockFlags.SYNC);
    }

    /**
     * Sets the block at the specified coordinates, with the given block type.
     *
     * @param blockVec the position
     * @param state    the block state to set
     * @return true if the block was successfully set, false otherwise
     */
    @Override
    public boolean set(BlockVec blockVec, BlockProperties state, int flags) {
        this.checkThread();

        return this.set(blockVec.x(), blockVec.y(), blockVec.z(), state, flags);
    }

    /**
     * Sets the block at the specified coordinates, with the given block type.
     *
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @param z     the z-coordinate
     * @param block the block type to set
     * @return true if the block was successfully set, false otherwise
     */
    @Override
    public boolean set(int x, int y, int z, BlockProperties block) {
        return set(x, y, z, block, BlockFlags.UPDATE | BlockFlags.SYNC);
    }

    /**
     * Gets a block at the specified coordinates.
     *
     * @param pos the position
     * @return the block at the specified coordinates
     */
    @Override
    public @NotNull BlockProperties get(BlockVec pos) {
        this.checkThread();

        return this.get(pos.x(), pos.y(), pos.z());
    }

    /**
     * Gets a block at the specified coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     * @return the block at the specified coordinates
     */
    @Override
    public @NotNull BlockProperties get(int x, int y, int z) {
        this.checkThread();

        Chunk chunkAt = this.getChunkAt(x, y, z);
        BlockVec blockVec = new BlockVec(x, y, z);
        if (chunkAt == null) return Blocks.AIR.createMeta();
        if (!chunkAt.ready) return Blocks.AIR.createMeta();

        BlockVec cp = World.toLocalBlockVec(x, y, z);
        return chunkAt.getFast(cp.x(), cp.y(), cp.z());
    }

    protected abstract void checkThread();

    /**
     * Converts a block position to a chunk space block position.
     *
     * @param pos the world space block position
     * @return the block position in chunk space
     */
    public static BlockVec toLocalBlockVec(BlockVec pos) {
        return World.toLocalBlockVec(pos.x(), pos.y(), pos.z());
    }

    /**
     * Converts a block position to a chunk space block position.
     *
     * @param x the x coordinate in world space
     * @param y the y coordinate in world space
     * @param z the z coordinate in world space
     * @return the block position in chunk space
     */
    public static BlockVec toLocalBlockVec(int x, int y, int z) {
        int cx = x % World.CHUNK_SIZE;
        int cy = y % (World.CHUNK_HEIGHT + 1);
        int cz = z % World.CHUNK_SIZE;

        if (cx < 0) cx += World.CHUNK_SIZE;
        if (cz < 0) cz += World.CHUNK_SIZE;

        return new BlockVec(cx, cy, cz);
    }

    /**
     * Converts a block position to a chunk space block position.
     *
     * @param x   the x coordinate in world space
     * @param y   the y coordinate in world space
     * @param z   the z coordinate in world space
     * @param tmp a temporary vector to store the result
     * @return the block position in chunk space
     */
    public static Vec3i toLocalBlockVec(int x, int y, int z, Vec3i tmp) {
        tmp.x = x % World.CHUNK_SIZE;
        tmp.y = y % (World.CHUNK_HEIGHT + 1);
        tmp.z = z % World.CHUNK_SIZE;

        if (tmp.x < 0) tmp.x += World.CHUNK_SIZE;
        if (tmp.z < 0) tmp.z += World.CHUNK_SIZE;

        return tmp;
    }

    /**
     * Converts a world space chunk position to a region space chunk
     *
     * @param x the x coordinate in world space
     * @param z the z coordinate in world space
     * @return the chunk position in region space
     */
    public static ChunkVec toLocalChunkVec(int x, int z) {
        int cx = x % World.REGION_SIZE;
        int cz = z % World.REGION_SIZE;

        if (cx < 0) cx += World.REGION_SIZE;
        if (cz < 0) cz += World.REGION_SIZE;

        return new ChunkVec(cx, cz);
    }

    /**
     * Converts a world space chunk position to a region space chunk
     *
     * @param pos the chunk position in world space
     * @return the chunk position in region space
     */
    public static ChunkVec toLocalChunkVec(ChunkVec pos) {
        return World.toLocalChunkVec(pos.getX(), pos.getZ());
    }

    @Override
    public Chunk getChunk(int x, int z) {
        return this.getChunk(new ChunkVec(x, z));
    }

    @Override
    public Chunk getChunkAt(int x, int y, int z) {
        int chunkX = Math.floorDiv(x, World.CHUNK_SIZE);
        int chunkZ = Math.floorDiv(z, World.CHUNK_SIZE);

        if (this.isOutOfWorldBounds(x, y, z)) return null;

        ChunkVec chunkVec = new ChunkVec(chunkX, chunkZ);
        return this.getChunk(chunkVec);
    }

    @Override
    public abstract @Nullable Chunk getChunk(ChunkVec pos);

    @Override
    public Chunk getChunkAt(BlockVec pos) {
        return this.getChunkAt(pos.x(), pos.y(), pos.z());
    }

    @Override
    public boolean isOutOfWorldBounds(BlockVec pos) {
        return pos.y() < World.WORLD_DEPTH || pos.y() >= World.WORLD_HEIGHT
               || pos.x() < -30000000 || pos.x() > 30000000
               || pos.z() < -30000000 || pos.z() > 30000000;
    }

    @Override
    public boolean isOutOfWorldBounds(int x, int y, int z) {
        return y < World.WORLD_DEPTH || y >= World.WORLD_HEIGHT - 1
               || x < -30000000 || x > 30000000
               || z < -30000000 || z > 30000000;
    }

    /**
     * Get the highest block in a column.
     *
     * @param x the x coordinate of the column
     * @param z the z coordinate of the column
     * @return The highest block in the column, or -1 if the chunk isn't loaded.
     */
    @Override
    public int getHighest(int x, int z) {
        Chunk chunkAt = this.getChunkAt(x, 0, z);
        if (chunkAt == null) return Integer.MIN_VALUE;

        // FIXME: Optimize by using a heightmap.
        for (int y = World.CHUNK_HEIGHT - 1; y > 0; y--) {
            if (!this.get(x, y, z).isAir()) return y + 1;
        }
        return 0;
    }

    @Override
    public void setColumn(int x, int z, BlockProperties block) {
        this.setColumn(x, z, World.CHUNK_HEIGHT, block);
    }

    @Override
    public void setColumn(int x, int z, int maxY, BlockProperties block) {
        if (this.getChunkAt(x, maxY, z) == null) return;

        // FIXME optimize
        for (; maxY > 0; maxY--) {
            this.set(x, maxY, z, block);
        }
    }

    /**
     * Sets the specified block in a 3D area defined by the given coordinates and dimensions.
     *
     * @param x      the x-coordinate of the starting point
     * @param y      the y-coordinate of the starting point
     * @param z      the z-coordinate of the starting point
     * @param width  the width of the 3D area
     * @param height the height of the 3D area
     * @param depth  the depth of the 3D area
     * @param block  the block to be set in the specified area
     * @return a {@link CompletableFuture} representing the asynchronous operation
     * @see #set(int, int, int, BlockProperties)
     * @see #setColumn(int, int, BlockProperties)
     */
    @Override
    public CompletableFuture<Void> set(int x, int y, int z, int width, int height, int depth, BlockProperties block) {
        return CompletableFuture.runAsync(() -> {
            int curX = x, curY = y, curZ = z;
            int startX = Math.max(curX, 0);
            int startY = Math.max(curY, 0);
            int startZ = Math.max(curZ, 0);
            int endX = Math.min(width, curX + width);
            int endY = Math.min(height, curY + height);
            int endZ = Math.min(depth, curZ + depth);

            // FIXME optimize
            for (curY = startY; curY < endY; curY++) {
                for (curZ = startZ; curZ < endZ; curZ++) {
                    for (curX = startX; curX < endX; curX++) {
                        int blkX = curX;
                        int blkY = curY;
                        int blkZ = curZ;
                        QuantumServer.invoke(() -> this.set(blkX, blkY, blkZ, block));
                    }
                }
            }
        });
    }

    @Override
    public abstract Collection<? extends Chunk> getLoadedChunks();

    @Override
    public boolean isChunkInvalidated(Chunk chunk) {
        return this.invalidatedChunks.contains(chunk.getPos());
    }

    @Override
    @ApiStatus.Internal
    public void updateNeighbours(Chunk chunk) {
        ChunkVec pos = chunk.getPos();
        this.updateChunk(this.getChunk(new ChunkVec(pos.getX() - 1, pos.getZ())));
        this.updateChunk(this.getChunk(new ChunkVec(pos.getX() + 1, pos.getZ())));
        this.updateChunk(this.getChunk(new ChunkVec(pos.getX(), pos.getZ() - 1)));
        this.updateChunk(this.getChunk(new ChunkVec(pos.getX(), pos.getZ() + 1)));
    }

    @Override
    @ApiStatus.Internal
    public void updateChunkAndNeighbours(Chunk chunk) {
        ChunkVec pos = chunk.getPos();
        this.updateChunk(chunk);
        this.updateNeighbours(chunk);
    }

    @Override
    @ApiStatus.Internal
    public void updateChunk(@Nullable Chunk chunk) {
        if (chunk == null) return;
        this.invalidatedChunks.add(chunk.getPos());
    }

    /**
     * Spawns an entity.
     * <p>
     * <b>NOTE:</b> This method is obsolete, {@link #spawn(Entity, MapType)} exists with more functionality.
     *
     * @param entity The entity to spawn
     * @return The spawned entity
     */
    @Override
    @ApiStatus.Obsolete
    public <T extends Entity> T spawn(T entity) {
        Preconditions.checkNotNull(entity, "Cannot spawn null entity");

        // Set the entity ID
        this.setEntityId(entity);

        // Prepare the entity for spawn
        entity.onPrepareSpawn(new MapType());

        // Add the entity to the map of entities
        this.entitiesById.put(entity.getId(), entity);
        return entity;
    }

    /**
     * Spawns an entity with the given spawn data.
     *
     * @param entity The entity to spawn
     * @param spawnData The data for spawning the entity
     * @return The spawned entity
     */
    @Override
    public <T extends Entity> T spawn(T entity, MapType spawnData) {
        // Check if entity is not null
        Preconditions.checkNotNull(entity, "Cannot spawn null entity");

        // Check if spawn data is not null
        Preconditions.checkNotNull(spawnData, "Cannot spawn entity with null spawn data");

        // Set the entity ID
        this.setEntityId(entity);

        // Prepare the entity for spawn
        entity.onPrepareSpawn(spawnData);

        // Add the entity to the map of entities by ID
        this.entitiesById.put(entity.getId(), entity);

        return entity;
    }

    private <T extends Entity> void setEntityId(T entity) {
        Preconditions.checkNotNull(entity, "Cannot set entity id for null entity");
        int oldId = entity.getId();
        if (oldId > 0 && this.entitiesById.containsKey(oldId)) {
            throw new IllegalStateException("Entity already spawned: " + entity);
        }
        int newId = oldId > 0 ? oldId : this.nextId();
        entity.setId(newId);
    }

    private int nextId() {
        return this.curId++;
    }

    /**
     * Despawns an entity.
     *
     * @param entity the entity to despawn
     */
    @Override
    public void despawn(Entity entity) {
        this.entitiesById.remove(entity.getId());
    }

    /**
     * Despawns the entity from the entitiesById map.
     *
     * @param id The ID of the entity to be removed.
     */
    @Override
    public void despawn(int id) {
        this.entitiesById.remove(id);
    }

    @Override
    public Entity getEntity(int id) {
        return this.entitiesById.get(id);
    }

    /**
     * Collision detection against blocks.
     *
     * @param box          The bounding box of an entity.
     * @param collideFluid If true, will check for fluid collision.
     * @return A list of bounding boxes that got collided with.
     */
    @Override
    public List<BoundingBox> collide(BoundingBox box, boolean collideFluid) {
        List<BoundingBox> boxes = new ArrayList<>();
        int xMin = (int) Math.floor(box.min.x);
        int xMax = (int) Math.floor(box.max.x);
        int yMin = (int) Math.floor(box.min.y);
        int yMax = (int) Math.floor(box.max.y);
        int zMin = (int) Math.floor(box.min.z);
        int zMax = (int) Math.floor(box.max.z);

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    BlockProperties block = this.get(x, y, z);
                    if (block.hasCollider() && (!collideFluid || block.isFluid())) {
                        BoundingBox blockBox = block.getBoundingBox(x, y, z);
                        if (blockBox.intersects(box)) {
                            boxes.add(blockBox);
                        }
                    }
                }
            }
        }

        return boxes;
    }

    /**
     * Cleans up any used variables, executors, etc.
     */
    @Override
    @ApiStatus.Internal
    public void dispose() {
        this.disposed = true;
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(60, TimeUnit.SECONDS))
                throw new RuntimeException("Chunk builders executor failed to shutdown in time.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the amount of loaded chunks in the world.
     */
    public abstract int getTotalChunks();

    /**
     * Fills the crash log with information about the world.
     * <p style="color: red;">NOTE: Internal API!</p>
     *
     * @param crashLog the crash log
     */
    @ApiStatus.Internal
    public void fillCrashInfo(CrashLog crashLog) {
        // Create a new CrashCategory for world details
        CrashCategory cat = new CrashCategory("World Details");
        // Add total chunks information to the crash category
        cat.add("Total chunks", this.getTotalChunks()); // Too many chunks?
        // Add rendered chunks information to the crash category
        cat.add("Rendered chunks", this.renderedChunks); // Chunk render overflow?
        // Add seed information to the crash category
        cat.add("Seed", this.seed); // For weird world generation glitches

        // Add the world details category to the crash log
        crashLog.addCategory(cat);
    }

    /**
     * Checks if the given bounding box intersects any entities.
     *
     * @param boundingBox The bounding box to check with.
     * @return {@code true} if the bounding box intersects any entities, {@code false} otherwise.
     */
    @Override
    public boolean intersectEntities(BoundingBox boundingBox) {
        for (Entity entity : this.entitiesById.values())
            if (entity.getBoundingBox().intersects(boundingBox)) return true;

        return false;
    }

    /**
     * Start breaking a block at the given position.
     *
     * @param breaking the position of the block.
     * @param breaker  the player breaking the block.
     */
    @Override
    public void startBreaking(BlockVec breaking, Player breaker) {
        Chunk chunk = this.getChunkAt(breaking);
        if (chunk == null) return;
        BlockVec localBlockVec = World.toLocalBlockVec(breaking);
        chunk.startBreaking(localBlockVec.x(), localBlockVec.y(), localBlockVec.z());
    }

    /**
     * Continue breaking a block at the given position.
     *
     * @param breaking the position of the block.
     * @param amount   the amount of breaking progress to make.
     * @param breaker  the player breaking the block.
     * @return A {@link BreakResult} which indicates the current status of the block breaking.
     */
    @Override
    public BreakResult continueBreaking(BlockVec breaking, float amount, Player breaker) {
        Chunk chunk = this.getChunkAt(breaking);
        if (chunk == null) return BreakResult.FAILED;
        BlockVec localBlockVec = World.toLocalBlockVec(breaking);
        BlockProperties block = this.get(breaking);

        if (block.isAir()) return BreakResult.FAILED;

        return chunk.continueBreaking(localBlockVec.x(), localBlockVec.y(), localBlockVec.z(), amount);
    }

    /**
     * Stop breaking a block at the given position.
     *
     * @param breaking the position of the block.
     * @param breaker  the player breaking the block.
     */
    @Override
    public void stopBreaking(BlockVec breaking, Player breaker) {
        Chunk chunk = this.getChunkAt(breaking);
        if (chunk == null) return;
        BlockVec localBlockVec = World.toLocalBlockVec(breaking);
        chunk.stopBreaking(localBlockVec.x(), localBlockVec.y(), localBlockVec.z());
    }

    /**
     * Get the break progress of a block at the given position.
     *
     * @param pos the position of the block.
     * @return The break progress of the block, or -1.0 if the block isn't being mined.
     */
    @Override
    public float getBreakProgress(BlockVec pos) {
        Chunk chunk = this.getChunkAt(pos);
        if (chunk == null) return -1.0F;
        BlockVec localBlockVec = World.toLocalBlockVec(pos);
        return chunk.getBreakProgress(localBlockVec.x(), localBlockVec.y(), localBlockVec.z());
    }

    /**
     * @return thr world seed, which is the base seed of the whole world.
     */
    @Override
    public long getSeed() {
        return this.seed;
    }

    /**
     * Set the spawn point of the world.
     *
     * @param spawnX the x position of the spawn point
     * @param spawnZ the z position of the spawn point
     */
    @Override
    public void setSpawnPoint(int spawnX, int spawnZ) {
        this.spawnPoint.set(spawnX, this.getHighest(spawnX, spawnZ), spawnZ);
    }

    /**
     * Check if the given chunk is a spawn chunk.
     *
     * @param pos the chunk position
     * @return {@code true} if the chunk is a spawn chunk, {@code false} otherwise
     */
    @Override
    public boolean isSpawnChunk(ChunkVec pos) {
        int x = pos.getX() * 16;
        int z = pos.getZ() * 16;

        return this.spawnPoint.x - 1 <= x && this.spawnPoint.x + 1 >= x &&
               this.spawnPoint.z - 1 <= z && this.spawnPoint.z + 1 >= z;
    }

    /**
     * Get the spawn point of the world.
     *
     * @return the spawn point
     */
    @Override
    public BlockVec getSpawnPoint() {
        int highest = this.getHighest(this.spawnX, this.spawnZ);
        int spawnY = 0;
        if (highest != Integer.MIN_VALUE)
            spawnY = highest;

        return new BlockVec(this.spawnX, spawnY, this.spawnZ);
    }

    @Override
    public int getChunksLoaded() {
        return this.getTotalChunks();
    }

    @Override
    public boolean isDisposed() {
        return this.disposed;
    }

    @Override
    public void onChunkUpdated(Chunk chunk) {
        this.invalidatedChunks.remove(chunk.getPos());
    }

    /**
     * Play a sound at the given location.
     *
     * @param sound the sound event to play.
     * @param x     the x position of the sound.
     * @param y     the y position of the sound.
     * @param z     the z position of the sound.
     */
    @Override
    public void playSound(SoundEvent sound, double x, double y, double z) {

    }

    @Override
    @ApiStatus.Internal
    public void closeMenu(ContainerMenu containerMenu) {
        if (!this.menus.contains(containerMenu)) return;
        this.menus.remove(containerMenu);
    }

    @Override
    @ApiStatus.Internal
    public void openMenu(ContainerMenu containerMenu) {
        if (this.menus.contains(containerMenu)) return;
        this.menus.add(containerMenu);
    }

    /**
     * @return true if the world is running on the server, false otherwise.
     */
    @Override
    public boolean isServerSide() {
        return !this.isClientSide();
    }

    @Override
    public Biome getBiome(BlockVec pos) {
        BlockVec localVec = World.toLocalBlockVec(pos);
        Chunk chunk = this.getChunkAt(pos);
        if (chunk == null) return null;
        return chunk.getBiome(localVec.x(), localVec.z());
    }

    @Override
    public DimensionInfo getDimension() {
        return this.info;
    }

    @Override
    public Array<Entity> getEntities() {
        return this.entitiesById.values().toArray();
    }

    @Override
    public @NotNull <T extends Entity> Iterable<Entity> getEntitiesByClass(Class<T> clazz) {
        return this.entitiesById.values().toArray().select(clazz::isInstance);
    }

    @Override
    public @NotNull UUID getUID() {
        return this.uid;
    }

    @Override
    public boolean set(int x, int y, int z, @NotNull BlockProperties block,
                       @MagicConstant(flagsFromClass = BlockFlags.class) int flags) {
        this.checkThread();

        BlockEvents.SET_BLOCK.factory().onSetBlock(this, new BlockVec(x, y, z), block);

        Chunk chunk = this.getChunkAt(x, y, z);
        if (chunk == null) return false;

        BlockVec cp = World.toLocalBlockVec(x, y, z);
        return chunk.set(cp.x(), cp.y(), cp.z(), block);
    }

    @Override
    public void setBlockEntity(BlockVec pos, BlockEntity blockEntity) {
        Chunk chunk = this.getChunkAt(pos);
        if (chunk == null) return;

        chunk.setBlockEntity(World.toLocalBlockVec(pos), blockEntity);
    }

    @Override
    public BlockEntity getBlockEntity(BlockVec pos) {
        Chunk chunk = this.getChunkAt(pos);
        if (chunk == null) return null;

        BlockVec localVec = World.toLocalBlockVec(pos);
        return chunk.getBlockEntity(localVec.x(), localVec.y(), localVec.z());
    }

    @Override
    public void drop(ItemStack itemStack, Vec3d position) {
        drop(itemStack, position, new Vec3d());
    }

    @Override
    public void drop(ItemStack itemStack, Vec3d position, Vec3d velocity) {
        if (this.isClientSide()) {
            CommonConstants.LOGGER.warn("Tried to drop an item on the client side!");
            return;
        }

        this.spawn(new DroppedItem(this, itemStack, position, velocity), new MapType());
    }

    @Override
    public Iterable<Entity> entitiesWithinDst(Entity entity, int distance) {
        return entitiesById.values().toArray().select(entity1 -> entity1.distanceTo(entity) <= distance);
    }

    @Override
    public Iterable<Entity> collideEntities(Entity droppedItem, BoundingBox ext) {
        return entitiesById.values().toArray().select(entity -> entity.getBoundingBox().intersects(ext));
    }

    @Override
    public void spawnParticles(ParticleType particleType, Vec3d position, Vec3d motion, int count) {

    }

    @Override
    public boolean destroyBlock(BlockVec breaking, @Nullable Player breaker) {
        BlockProperties blockProperties = get(breaking);

        if (breaker != null && BlockEvents.BREAK_BLOCK.factory().onBreakBlock(this, breaking, blockProperties, breaker).isCanceled()) {
            stopBreaking(breaking, breaker);
        }

        blockProperties.onDestroy(this, breaking, breaker);
        set(breaking, Blocks.AIR.createMeta(), BlockFlags.UPDATE);
        return true;
    }

    @Override
    public int getBlockLight(int x, int y, int z) {
        return 0;
    }

    @Override
    public void setBlockLight(int x, int y, int z, int intensity) {

    }

    @Override
    public void updateLightSources(Vec3i offset, ObjectMap<Vec3i, LightSource> lights) {

    }
}