package dev.ultreon.quantum.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.Logger;
import dev.ultreon.quantum.LoggerFactory;
import dev.ultreon.quantum.Promise;
import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.api.events.block.BlockBrokenEvent;
import dev.ultreon.quantum.api.events.block.BlockSetEvent;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.entity.DroppedItem;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.Menu;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.util.Utils;
import dev.ultreon.quantum.ubo.types.LongType;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.gen.StructureData;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.quantum.world.structure.Structure;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.ChunkVecSpace;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Represents the world (also known as a dimension) in the game with various attributes and manipulation methods.
 */
@SuppressWarnings({"UnusedReturnValue", "unused", "GDXJavaUnsafeIterator"})
@ApiStatus.NonExtendable
@ParametersAreNonnullByDefault
public abstract class World extends GameObject implements Disposable, WorldAccess {
    public static final int CS = 16;
    public static final int CS_2 = CS * CS;
    public static final int CS_3 = CS_2 * CS;

    public static final int REGION_SIZE = 16;
    public static final NamespaceID OVERWORLD = new NamespaceID("overworld");
    public static final int SEA_LEVEL = 64;
    public static final int REGION_DATA_VERSION = 0;

    protected static final Logger LOGGER = LoggerFactory.getLogger(World.class);

    protected final BlockVec spawnPoint = new BlockVec(BlockVecSpace.WORLD);
    protected final long seed;
    private int renderedChunks;

    protected final IntMap<Entity> entitiesById = new IntMap<>();
    private int curId;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<ChunkVec> alwaysLoaded = new ArrayList<>();

    boolean disposed;
    private final Set<ChunkVec> invalidatedChunks = Collections.synchronizedSet(new LinkedHashSet<>());
    private final List<Menu> menus = new ArrayList<>();
    private final RegistryKey<DimensionInfo> info = DimensionInfo.OVERWORLD; // TODO WIP
    protected UUID uid = Utils.ZEROED_UUID;
    protected int spawnX;
    protected int spawnZ;
    private final Map<ChunkVec, Heightmap> motionBlockingHeightMaps = new ConcurrentHashMap<>();
    private final Map<ChunkVec, Heightmap> worldSurfaceHeightMaps = new ConcurrentHashMap<>();
    protected StructureData structureData = new StructureData();

    protected World() {
        // Shh, the original seed was 512.
        this(new Random().nextLong());
    }

    protected World(long seed) {
        this.seed = seed;
    }

    public World(@Nullable LongType seed) {
        this(seed == null ? new Random().nextLong() : seed.getValue());
    }

    @Deprecated
    public static ChunkVec toChunkVec(int x, int y, int z) {
        return new BlockVec(x, y, z, BlockVecSpace.WORLD).chunk();
    }

    protected abstract int getRenderDistance();

    protected boolean shouldStayLoaded(ChunkVec pos) {
        return this.isAlwaysLoaded(pos);
    }

    @Override
    public boolean isAlwaysLoaded(ChunkVec pos) {
        return this.alwaysLoaded.contains(pos);
    }

    @Override
    public boolean unloadChunk(@NotNull ChunkVec chunkVec) {
        this.checkThread();

        Chunk chunk = this.getChunk(chunkVec);
        if (chunk == null) return true;
        return this.unloadChunk(chunk, chunkVec);
    }

    @Override
    public abstract boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkVec pos);

    @Override
    public @NotNull EntityHit rayCastEntity(Ray ray) {
        return rayCastEntity(ray, 5);
    }

    @Override
    public @NotNull EntityHit rayCastEntity(Ray ray, float distance) {
        EntityHit result = new EntityHit(ray, distance);
        Stream<Entity> entitiesWithin = getEntitiesWithin(new BoundingBox(ray.origin, ray.origin.add(ray.direction.cpy().scl(distance))));
        entitiesWithin.forEach(entity -> {
            double curDistance = ray.origin.dst(entity.getPosition());
            if (curDistance > distance) return;
            Vec intersection = new Vec();
            if (Intersector.intersectRayBounds(ray, entity.getBoundingBox(), intersection) && (result.getEntity() == null || curDistance < result.getDistance() && curDistance < result.getDistanceMax())) {
                result.entity = entity;
                result.distance = (float) curDistance;
                result.position = intersection;
                result.collide = true;
            }
        });

        return result;
    }

    @Override
    public @NotNull EntityHit rayCastEntity(Ray ray, float distance, Predicate<Entity> filter) {
        EntityHit result = new EntityHit(ray, distance);
        Stream<Entity> entitiesWithin = getEntitiesWithin(new BoundingBox(ray.origin, ray.origin.add(ray.direction.cpy().scl(distance))));
        entitiesWithin.forEach(entity -> {
            if (filter.test(entity)) {
                double curDistance = ray.origin.dst(entity.getPosition());
                if (curDistance > distance) return;
                if (result.getEntity() == null || curDistance < result.getDistance() && curDistance < result.getDistanceMax()) {
                    result.entity = entity;
                    result.distance = (float) curDistance;
                }
            }
        });

        if (result.getEntity() != null) {
            result.collide = true;
        }

        return result;
    }

    @Override
    public @NotNull EntityHit rayCastEntity(Ray ray, float distance, EntityType<?> type) {
        return rayCastEntity(ray, distance, e -> e.getType() == type);
    }

    @Override
    public @NotNull EntityHit rayCastEntity(Ray ray, float distance, Class<? extends Entity> type) {
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
    public boolean set(BlockVec blockVec, BlockState state) {
        return set(blockVec, state, BlockFlags.UPDATE | BlockFlags.SYNC);
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
    public boolean set(int x, int y, int z, BlockState block) {
        return set(x, y, z, block, BlockFlags.UPDATE | BlockFlags.SYNC);
    }

    /**
     * Gets a block at the specified coordinates.
     *
     * @param pos the position
     * @return the block at the specified coordinates
     */
    @Override
    public @NotNull BlockState get(BlockVec pos) {
        this.checkThread();

        return this.get(pos.getIntX(), pos.getIntY(), pos.getIntZ());
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
    public @NotNull BlockState get(int x, int y, int z) {
        this.checkThread();

        Chunk chunkAt = this.getChunkAt(x, y, z);
        BlockVec blockVec = new BlockVec(x, y, z, BlockVecSpace.WORLD);
        if (chunkAt == null) return Blocks.VOID_AIR.getDefaultState();
        if (!chunkAt.ready) return Blocks.VOID_AIR.getDefaultState();

        BlockVec cp = blockVec.chunkLocal();
        return chunkAt.getFast(cp.getIntX(), cp.getIntY(), cp.getIntZ());
    }

    protected abstract void checkThread();

    /**
     * Converts a block position to a chunk space block position.
     *
     * @param x the x coordinate in world space
     * @param y the y coordinate in world space
     * @param z the z coordinate in world space
     * @return the block position in chunk space
     */
    public static BlockVec toLocalBlockVec(int x, int y, int z) {
        BlockVec worldSpace = new BlockVec(x, y, z, BlockVecSpace.WORLD);
        return worldSpace.chunkLocal();
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
    public static BlockVec toLocalBlockVec(int x, int y, int z, Vec3i tmp) {
        BlockVec worldSpace = new BlockVec(x, y, z, BlockVecSpace.WORLD);
        return worldSpace.chunkLocal();
    }

    /**
     * Converts a world space chunk position to a region space chunk
     *
     * @param x the x coordinate in world space
     * @param z the z coordinate in world space
     * @return the chunk position in region space
     */
    public static ChunkVec toLocalChunkVec(int x, int y, int z) {
        ChunkVec worldSpace = new ChunkVec(x, y, z, ChunkVecSpace.WORLD);
        return worldSpace.regionLocal();
    }

    /**
     * Converts a world space chunk position to a region space chunk
     *
     * @param pos the chunk position in world space
     * @return the chunk position in region space
     */
    public static ChunkVec toLocalChunkVec(ChunkVec pos) {
        return pos.regionLocal();
    }

    @Override
    public @Nullable Chunk getChunk(int x, int y, int z) {
        return this.getChunk(new ChunkVec(x, y, z, ChunkVecSpace.WORLD));
    }

    @Override
    public @Nullable Chunk getChunkAt(int x, int y, int z) {
        BlockVec blockVec = new BlockVec(x, y, z, BlockVecSpace.WORLD);

        if (this.isOutOfWorldBounds(x, y, z)) return null;

        ChunkVec chunkVec = blockVec.chunk();
        return this.getChunk(chunkVec);
    }

    @Override
    public abstract @Nullable Chunk getChunk(ChunkVec pos);

    @Override
    public @Nullable Chunk getChunkAt(BlockVec pos) {
        return this.getChunkAt(pos.getIntX(), pos.getIntY(), pos.getIntZ());
    }

    @Override
    public boolean isOutOfWorldBounds(BlockVec pos) {
        return pos.getIntY() < -30000000 || pos.getIntY() > 30000000
               || pos.getIntX() < -30000000 || pos.getIntX() > 30000000
               || pos.getIntZ() < -30000000 || pos.getIntZ() > 30000000;
    }

    @Override
    public boolean isOutOfWorldBounds(int x, int y, int z) {
        return y < -30000000 || y > 30000000
               || x < -30000000 || x > 30000000
               || z < -30000000 || z > 30000000;
    }

    /**
     * Get the highest block in a column.
     *
     * @param x    the x coordinate of the column
     * @param z    the z coordinate of the column
     * @param type the heightmap type
     * @return The highest block in the column, or -1 if the chunk isn't loaded.
     */
    @Override
    public int getHeight(int x, int z, HeightmapType type) {
        BlockVec blockVec = new BlockVec(x, 0, z, BlockVecSpace.WORLD).chunkLocal();
        return this.heightMapAt(x, z, type).get(blockVec.x, blockVec.z);
    }

    @Override
    public Heightmap heightMapAt(int x, int z, HeightmapType type) {
        switch (type) {
            case MOTION_BLOCKING:
                return this.motionBlockingHeightMaps.computeIfAbsent(new BlockVec(x, 0, z, BlockVecSpace.WORLD).chunk(), vec -> new Heightmap(CS));
            case WORLD_SURFACE:
                return this.worldSurfaceHeightMaps.computeIfAbsent(new BlockVec(x, 0, z, BlockVecSpace.WORLD).chunk(), vec -> new Heightmap(CS));
            default:
                throw new IllegalArgumentException();
        }
    }

    public Heightmap heightMapAt(@NotNull ChunkVec vec, HeightmapType type) {
        switch (type) {
            case MOTION_BLOCKING:
                return this.motionBlockingHeightMaps.computeIfAbsent(new ChunkVec(vec.x, 0, vec.z, ChunkVecSpace.WORLD), v -> new Heightmap(CS));
            case WORLD_SURFACE:
                return this.worldSurfaceHeightMaps.computeIfAbsent(new ChunkVec(vec.x, 0, vec.z, ChunkVecSpace.WORLD), v -> new Heightmap(CS));
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    @Deprecated
    public void setColumn(int x, int z, BlockState block) {
        this.setColumn(x, z, 256, block);
    }

    @Override
    @Deprecated
    public void setColumn(int x, int z, int maxY, BlockState block) {
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
     * @return a {@link Promise} representing the asynchronous operation
     * @see #set(int, int, int, BlockState)
     * @see #setColumn(int, int, BlockState)
     */
    @Override
    public Promise<Void> set(int x, int y, int z, int width, int height, int depth, BlockState block) {
        return Promise.runAsync(() -> {
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
        return this.invalidatedChunks.contains(chunk.getVec());
    }

    @Override
    @ApiStatus.Internal
    public void updateNeighbours(Chunk chunk) {
        ChunkVec pos = chunk.getVec();
        if (pos.getSpace() != ChunkVecSpace.WORLD) throw new IllegalArgumentException("Chunk must be in world space");
        this.updateChunk(this.getChunk(pos.offset(-1, -1, 0)));
        this.updateChunk(this.getChunk(pos.offset(+1, -1, 0)));
        this.updateChunk(this.getChunk(pos.offset(0, -1, -1)));
        this.updateChunk(this.getChunk(pos.offset(0, -1, +1)));

        this.updateChunk(this.getChunk(pos.offset(-1, +1, 0)));
        this.updateChunk(this.getChunk(pos.offset(+1, +1, 0)));
        this.updateChunk(this.getChunk(pos.offset(0, +1, -1)));
        this.updateChunk(this.getChunk(pos.offset(0, +1, +1)));
    }

    @Override
    @ApiStatus.Internal
    public void updateChunkAndNeighbours(Chunk chunk) {
        ChunkVec pos = chunk.getVec();
        this.updateChunk(chunk);
        this.updateNeighbours(chunk);
    }

    @Override
    @ApiStatus.Internal
    public void updateChunk(@Nullable Chunk chunk) {
        if (chunk == null) return;
        this.invalidatedChunks.add(chunk.getVec());
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
     * @param entity    The entity to spawn
     * @param spawnData The data for spawning the entity
     * @return The spawned entity
     */
    @Override
    public <T extends Entity> T spawn(T entity, MapType spawnData) {
        // Check if entity is not null
        // Check if spawn data is not null
        // Set the entity ID
        this.setEntityId(entity);

        // Prepare the entity for spawn
        entity.onPrepareSpawn(spawnData);

        // Add the entity to the map of entities by ID
        this.entitiesById.put(entity.getId(), entity);

        return entity;
    }

    private <T extends Entity> void setEntityId(T entity) {        int oldId = entity.getId();
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
                    BlockState block = this.get(x, y, z);
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
        super.dispose();
        this.disposed = true;
    }

    /**
     * @return the number of loaded chunks in the world.
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
        cat.add("Seed", this.seed); // For unusual world generation glitches

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
    @SuppressWarnings("GDXJavaUnsafeIterator")
    public boolean intersectEntities(BoundingBox boundingBox) {
        for (Entity entity : this.entitiesById.values().toArray().toArray(Entity.class))
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
        BlockVec localBlockVec = breaking.chunkLocal();
        chunk.startBreaking(localBlockVec.getIntX(), localBlockVec.getIntY(), localBlockVec.getIntZ());
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
        BlockVec localBlockVec = breaking.chunkLocal();
        BlockState block = this.get(breaking);

        if (block.isAir()) return BreakResult.FAILED;

        return chunk.continueBreaking(localBlockVec.getIntX(), localBlockVec.getIntY(), localBlockVec.getIntZ(), amount);
    }

    /**
     * Stop breaking a block at the given position.
     *
     * @param breaking the position of the block.
     * @param breaker  the player breaking the block.
     * @return {@code true} if the block was successfully stopped breaking, {@code false} otherwise.
     */
    @Override
    public boolean stopBreaking(BlockVec breaking, Player breaker) {
        Chunk chunk = this.getChunkAt(breaking);
        if (chunk == null) return false;
        BlockVec localBlockVec = breaking.chunkLocal();
        return chunk.stopBreaking(localBlockVec.getIntX(), localBlockVec.getIntY(), localBlockVec.getIntZ());
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
        BlockVec localBlockVec = pos.chunkLocal();
        return chunk.getBreakProgress(localBlockVec.getIntX(), localBlockVec.getIntY(), localBlockVec.getIntZ());
    }

    /**
     * @return the world seed, which is the base seed of the whole world.
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
        this.spawnPoint.set(spawnX, this.getHeight(spawnX, spawnZ), spawnZ);
    }

    /**
     * Check if the given chunk is a spawn chunk.
     *
     * @param pos the chunk position
     * @return {@code true} if the chunk is a spawn chunk, {@code false} otherwise
     */
    @Override
    public boolean isSpawnChunk(ChunkVec pos) {
        int x = pos.getIntX() * CS;
        int z = pos.getIntZ() * CS;

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
        int highest = this.getHeight(this.spawnX, this.spawnZ);
        int spawnY = 0;
        if (highest != Integer.MIN_VALUE)
            spawnY = highest;

        return new BlockVec(this.spawnX, spawnY, this.spawnZ, BlockVecSpace.WORLD);
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
        this.invalidatedChunks.remove(chunk.getVec());
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
    public RegistryKey<Biome> getBiome(BlockVec pos) {
        Chunk chunk = this.getChunkAt(pos);
        if (chunk == null) return null;
        BlockVec localVec = pos.chunkLocal();
        return chunk.getBiome(localVec.getIntX(), localVec.getIntZ());
    }

    @Override
    public RegistryKey<DimensionInfo> getDimension() {
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

    /**
     * Sets the block at the specified coordinates, with the given block type.
     *
     * @param position the position
     * @param block    the block state to set
     * @return true if the block was successfully set, false otherwise
     */
    @Override
    public boolean set(BlockVec position, @NotNull BlockState block,
                       @MagicConstant(flagsFromClass = BlockFlags.class) int flags) {
        return this.set(position.getIntX(), position.getIntY(), position.getIntZ(), block, flags);
    }

    @Override
    public boolean set(int x, int y, int z, @NotNull BlockState block,
                       @MagicConstant(flagsFromClass = BlockFlags.class) int flags) {
        this.checkThread();

        ModApi.getGlobalEventHandler().call(new BlockSetEvent(this, new BlockVec(x, y, z), block, flags));

        Chunk chunk = this.getChunkAt(x, y, z);
        if (chunk == null) return false;

        BlockVec localPos = toLocalBlockVec(x, y, z);
        return chunk.set(localPos.getIntX(), localPos.getIntY(), localPos.getIntZ(), block);
    }

    @Override
    public void setBlockEntity(BlockVec pos, BlockEntity blockEntity) {
        Chunk chunk = this.getChunkAt(pos);
        if (chunk == null) return;

        chunk.setBlockEntity(pos.chunkLocal(), blockEntity);
    }

    @Override
    public BlockEntity getBlockEntity(BlockVec pos) {
        Chunk chunk = this.getChunkAt(pos);
        if (chunk == null) return null;

        BlockVec localVec = pos.chunkLocal();
        return chunk.getBlockEntity(localVec.getIntX(), localVec.getIntY(), localVec.getIntZ());
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
        BlockState blockState = get(breaking);

        if (breaker != null && ModApi.getGlobalEventHandler().call(new BlockBrokenEvent(this, breaking, blockState, Blocks.AIR.getDefaultState(), null, breaker))) {
            stopBreaking(breaking, breaker);
        }

        blockState.onDestroy(this, breaking, breaker);
        set(breaking, Blocks.AIR.getDefaultState(), BlockFlags.UPDATE);
        return true;
    }

    @Override
    public int getBlockLight(int x, int y, int z) {
        return 0;
    }

    public int getLight(int x, int y, int z) {
        return 0xF0;
    }

    @Override
    public void setBlockLight(int x, int y, int z, int intensity) {
        // No-op
    }

    @Override
    public void updateLightSources(Vec3i offset, ObjectMap<Vec3i, LightSource> lights) {
        // No-op
    }

    public abstract boolean isLoaded(@NotNull Chunk chunk);

    public @Nullable Structure getClosebyStructureCoords(ServerWorld world, int x, int z) {
        List<BlockVec> list = new ArrayList<>();
        return this.structureData.getStructureAt(x, world.getHeight(x, z), z);
    }
}