package dev.ultreon.quantum.world;

import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.collection.PaletteStorage;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.server.ServerDisposable;
import dev.ultreon.quantum.util.BlockMetaPredicate;
import dev.ultreon.quantum.util.PosOutOfBoundsException;
import dev.ultreon.quantum.util.ValidationError;
import dev.ultreon.quantum.world.gen.TreeData;
import dev.ultreon.quantum.world.gen.biome.Biomes;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

import static com.ultreon.libs.commons.v0.Mth.lerp;
import static dev.ultreon.quantum.world.World.*;

/**
 * Represents a chunk in the world.
 * <p style="color: red;">NOTE: This class isn't meant to be extended</p>
 * <p style="color: red;">NOTE: This class isn't thread safe</p>
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
@NotThreadSafe
@ApiStatus.NonExtendable
public abstract class Chunk implements ServerDisposable, ChunkAccess {
    public static final int VERTEX_SIZE = 6;
    private final ChunkPos pos;
    final Map<BlockPos, Float> breaking = new HashMap<>();
    protected final Object lock = new Object();
    protected boolean active;
    protected boolean ready;

    /**
     * @deprecated Use {@link World#CHUNK_SIZE} instead
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    public final int size = CHUNK_SIZE;
    /**
     * @deprecated Use {@link World#CHUNK_HEIGHT} instead
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    public final int height = CHUNK_HEIGHT;
    protected final Vec3i offset;
    @MonotonicNonNull
    @ApiStatus.Internal
    public TreeData treeData;
    private boolean disposed;
    private final World world;

    public final Storage<BlockProperties> storage;
    protected final LightMap lightMap = new LightMap(CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE);
    protected final HeightMap heightMap = new HeightMap(CHUNK_SIZE);
    public final Storage<Biome> biomeStorage;

    protected static final int MAX_LIGHT_LEVEL = 15;
    protected static final float[] lightLevelMap = new float[Chunk.MAX_LIGHT_LEVEL + 1];

    static {
        for (int i = 0; i <= Chunk.MAX_LIGHT_LEVEL; i++) {
            double lerp = lerp(0.15f, 1.5f, (double) i / Chunk.MAX_LIGHT_LEVEL);
            Chunk.lightLevelMap[i] = (float) lerp;
        }
    }

    private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();

    /**
     * @deprecated Use {@link #Chunk(World, ChunkPos)} instead@
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    protected Chunk(World world, int size, int height, ChunkPos pos) {
        this(world, size, height, pos, new PaletteStorage<>(BlockProperties.AIR, size * height * size));
    }

    /**
     * @deprecated Use {@link #Chunk(World, ChunkPos, Storage)} instead
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    protected Chunk(World world, int size, int height, ChunkPos pos, Storage<BlockProperties> storage) {
        this(world, size, height, pos, storage, new PaletteStorage<>(Biomes.PLAINS, 256));
    }

    /**
     * @deprecated Use {@link #Chunk(World, ChunkPos, Storage, Storage)} instead@
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    protected Chunk(World world, int ignoredSize, int ignoredHeight, ChunkPos pos, Storage<BlockProperties> storage, Storage<Biome> biomeStorage) {
        this(world, pos, storage, biomeStorage);
    }

    /**
     * Creates a new chunk.
     *
     * @param world the world the chunk is in.
     * @param pos   the chunk position.
     */
    protected Chunk(World world, ChunkPos pos) {
        this(world, pos, new PaletteStorage<>(BlockProperties.AIR, CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE));
    }

    /**
     * Creates a new chunk.
     *
     * @param world   the world the chunk is in.
     * @param pos     the chunk position.
     * @param storage the block storage.
     */
    protected Chunk(World world, ChunkPos pos, Storage<BlockProperties> storage) {
        this(world, pos, storage, new PaletteStorage<>(Biomes.PLAINS, 256));
    }

    /**
     * Creates a new chunk.
     *
     * @param world        the world the chunk is in.
     * @param pos          the chunk position.
     * @param storage      the block storage.
     * @param biomeStorage the biome storage
     */
    protected Chunk(World world, ChunkPos pos, Storage<BlockProperties> storage, Storage<Biome> biomeStorage) {
        this.world = world;

        this.offset = new Vec3i(pos.x() * CHUNK_SIZE, WORLD_DEPTH, pos.z() * CHUNK_SIZE);

        this.pos = pos;
        this.storage = storage;
        this.biomeStorage = biomeStorage;
    }

    /**
     * Decodes a block from a Packet Buffer object.
     *
     * @param buffer The input data.
     * @return The decoded block data.
     */
    public static Block decodeBlock(PacketIO buffer) {
        int id = buffer.readInt();
        return Registries.BLOCK.byId(id);
    }

    /**
     * Decodes a block from a UBO object.
     *
     * @param data The input data.
     * @return The decoded block data.
     * @deprecated Use {@link BlockProperties#load(MapType)} instead
     */
    @Deprecated
    public static BlockProperties loadBlock(MapType data) {
        return BlockProperties.load(data);
    }

    /**
     * Gets the block at the given coordinates. Note that this method is not thread safe.
     *
     * @param pos the position of the block
     * @return the block at the given coordinates
     */
    public BlockProperties get(Vec3i pos) {
        if (this.disposed) return Blocks.AIR.createMeta();
        return this.get(pos.x, pos.y, pos.z);
    }

    /**
     * Gets the block at the given coordinates. Note that this method is not thread safe.
     *
     * @param pos the position of the block
     * @return the block at the given coordinates
     */
    public BlockProperties get(BlockPos pos) {
        if (this.disposed) return Blocks.AIR.createMeta();
        return this.get(pos.x(), pos.y(), pos.z());
    }

    /**
     * Gets the block at the given coordinates. Note that this method is not thread safe.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @return the block at the given coordinates
     */
    public BlockProperties get(int x, int y, int z) {
        if (this.disposed) return Blocks.AIR.createMeta();
        if (this.isOutOfBounds(x, y, z)) return Blocks.AIR.createMeta();
        return this.getFast(x, y, z);
    }

    /**
     * Gets the block at the given coordinates.
     * Note that this method is not thread safe.
     * <p>This method isn't checking for out of bounds, so be careful when using it.</p>
     *
     * @param pos the position of the block
     * @return the block at the given coordinates
     */
    public BlockProperties getFast(Vec3i pos) {
        return this.getFast(pos.x, pos.y, pos.z);
    }

    /**
     * Gets the block at the given coordinates.
     * Note that this method is not thread safe.
     * <p>This method isn't checking for out of bounds, so be careful when using it.</p>
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @return the block at the given coordinates
     */
    public BlockProperties getFast(int x, int y, int z) {
        if (this.disposed) return Blocks.AIR.createMeta();
        int dataIdx = this.getIndex(x, y, z);

        BlockProperties block = this.storage.get(dataIdx);
        return block == null ? Blocks.AIR.createMeta() : block;
    }

    /**
     * Sets the block at the given coordinates. Note that this method is not thread safe.
     *
     * @param pos   the position of the block
     * @param block the block to set
     */
    public void set(Vec3i pos, BlockProperties block) {
        this.set(pos.x, pos.y, pos.z, block);
    }

    /**
     * Sets the block at the given coordinates. Note that this method is not thread safe.
     *
     * @param x     the x coordinate of the block
     * @param y     the y coordinate of the block
     * @param z     the z coordinate of the block
     * @param block the block to set
     * @return true if the block was successfully set, false if setting the block failed
     */
    public boolean set(int x, int y, int z, BlockProperties block) {
        if (this.isOutOfBounds(x, y, z)) return false;
        return this.setFast(x, y, z, block);
    }

    /**
     * Sets the block at the given coordinates.
     * Note that this method is not thread safe.
     * <p>This method also isn't checking for out of bounds, so be careful when using it.</p>
     *
     * @param pos   the position of the block
     * @param block the block to set
     */
    public void setFast(Vec3i pos, BlockProperties block) {
        this.setFast(pos.x, pos.y, pos.z, block);
    }

    /**
     * Sets the block at the given coordinates.
     * Note that this method is not thread safe.
     * <p>This method also isn't checking for out of bounds, so be careful when using it.</p>
     *
     * @param x     the x coordinate of the block
     * @param y     the y coordinate of the block
     * @param z     the z coordinate of the block
     * @param block the block to set
     * @return true if the block was successfully set, false if setting the block failed
     */
    public boolean setFast(int x, int y, int z, BlockProperties block) {
        if (this.disposed) return false;
        int index = this.getIndex(x, y, z);

        this.breaking.remove(new BlockPos(x, y, z));
        this.storage.set(index, block);

        if (this.heightMap.get(x, z) < y && !block.isAir()) {
            this.heightMap.set(x, z, (short) y);
        } else if (this.heightMap.get(x, z) == y && block.isAir()) {
            int curY;
            for (curY = y; curY >= 0; curY--) {
                if (!this.getFast(x, curY, z).isAir()) {
                    this.heightMap.set(x, z, (short) (curY + 1));
                    break;
                }
            }

            if (curY < 0) {
                this.heightMap.set(x, z, (short) 0);
            }
        }

        return true;
    }

    private int getIndex(int x, int y, int z) {
        if (x >= 0 && x < CHUNK_SIZE && y >= 0 && y < CHUNK_HEIGHT && z >= 0 && z < CHUNK_SIZE) {
            return z * (CHUNK_SIZE * CHUNK_HEIGHT) + y * CHUNK_SIZE + x;
        }
        return -1; // Out of bounds
    }

    /**
     * Checks if the given coordinates are out of bounds
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return true if the coordinates are out of bounds
     */
    protected boolean isOutOfBounds(int x, int y, int z) {
        return x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_HEIGHT || z < 0 || z >= CHUNK_SIZE;
    }

    /**
     * Disposes the chunk.
     * When the chunk is disposed, the chunk will no longer be usable.
     * <p>NOTE: This method is not thread safe.</p>
     * <p>NOTE: This is internal API, do not use it if you don't know what you are doing.</p>
     *
     * @exception ValidationError if the chunk is already disposed
     */
    @Override
    public void dispose() {
        synchronized (this.lock) {
            if (this.disposed) throw new ValidationError("Chunk is already disposed");
            this.disposed = true;
            this.ready = false;

            if (this.storage instanceof ServerDisposable disposable) {
                disposable.dispose();
            }
        }
    }

    @Override
    public String toString() {
        return "Chunk[x=" + this.getPos().x() + ", z=" + this.getPos().z() + "]";
    }

    public Vec3i getOffset() {
        return this.offset.cpy();
    }

    float getBreakProgress(float x, float y, float z) {
        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
        Float v = this.breaking.get(pos);
        if (v != null) {
            return v;
        }
        return -1.0F;
    }

    public void startBreaking(int x, int y, int z) {
        this.breaking.put(new BlockPos(x, y, z), 0.0F);
    }

    public void stopBreaking(int x, int y, int z) {
        this.breaking.remove(new BlockPos(x, y, z));
    }

    public BreakResult continueBreaking(int x, int y, int z, float amount) {
        BlockPos pos = new BlockPos(x, y, z);
        Float v = this.breaking.computeIfPresent(pos, (pos1, cur) -> Mth.clamp(cur + amount, 0, 1));
        if (v != null && v == 1.0F) {
            this.breaking.remove(pos);
            return BreakResult.BROKEN;
        }
        return BreakResult.CONTINUE;
    }

    public Map<BlockPos, Float> getBreaking() {
        return Collections.unmodifiableMap(this.breaking);
    }

    public boolean isReady() {
        return this.ready;
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    @ApiStatus.Internal
    public void onUpdated() {
        this.world.onChunkUpdated(this);
    }

    public World getWorld() {
        return this.world;
    }

    public boolean isActive() {
        return this.active;
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    /**
     * Find the highest block at the given position.
     *
     * @param x the x coordinate
     * @param z the z coordinate
     * @return The highest block Y coordinate.
     */
    public int getHighest(int x, int z) {
//        return this.heightMap.get(x, z);
        for (int y = CHUNK_HEIGHT - 1; y >= 1; y--) {
            if (!this.getFast(x, y, z).isAir()) {
                return y;
            }
        }
        return 64;
    }

    /**
     * Find the highest block at the given position.
     *
     * @param x the x coordinate
     * @param z the z coordinate
     * @return The highest block Y coordinate.
     */
    public int getHighest(int x, int z, BlockMetaPredicate blockPredicate) {
//        return this.heightMap.get(x, z);
        for (int y = CHUNK_HEIGHT - 1; y >= 1; y--) {
            if (!blockPredicate.test(this.getFast(x, y, z))) {
                return y;
            }
        }
        return 64;
    }

    /**
     * Find an empty space above the given position.
     *
     * @param x The x position.
     * @param y The y position to use as base.
     * @param z The z position.
     * @return The found position.
     */
    public int ascend(int x, int y, int z) {
        for (; y < CHUNK_HEIGHT; y++) {
            if (this.getFast(x, y, z).isAir()) {
                return y;
            }
        }
        return CHUNK_HEIGHT;
    }

    /**
     * Find an empty space with a given {@code height} above the given position.
     *
     * @param x      The x position.
     * @param y      The y position to use as base.
     * @param z      The z position.
     * @param height The height of the space.
     * @return The found position.
     */
    public int ascend(int x, int y, int z, int height) {
        for (; y < CHUNK_HEIGHT; y++) {
            if (!this.getFast(x, y, z).isAir()) continue;

            for (int i = 0; i < height; i++) {
                if (this.getFast(x, y + i, z).isAir()) return y;
            }
        }
        return CHUNK_HEIGHT;
    }

    public void setTreeData(TreeData treeData) {
        if (this.treeData != null) return;

        this.treeData = treeData;
    }

    protected int toFlatIndex(int x, int z) {
        return x + z * CHUNK_SIZE;
    }

    /**
     * Get the biome at the given position.
     *
     * @param x the x position in the chunk
     * @param ignoredY the y position in the chunk
     * @param z the z position in the chunk
     * @return the biome at the given position
     * @deprecated deprecated until biomes are 3 dimensional
     */
    @Deprecated
    public Biome getBiome(int x, int ignoredY, int z) {
        int index = this.toFlatIndex(x, z);
        return this.biomeStorage.get(index);
    }

    /**
     * Get the biome at the given position.
     *
     * @param x the x column position in the chunk
     * @param z the z column position in the chunk
     * @return the biome at the given position
     */
    public Biome getBiome(int x, int z) {
        int index = this.toFlatIndex(x, z);
        return this.biomeStorage.get(index);
    }

    /**
     * @param o the object to compare with
     * @return {@code true} if the objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return Objects.equals(this.pos, chunk.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pos);
    }

    /**
     * Get the sunlight level at the given position.
     *
     * @param x the x position in the chunk
     * @param y the y position in the chunk
     * @param z the z position in the chunk
     * @return the sunlight level at the given position between 0 and 15
     * @throws PosOutOfBoundsException if the position is out of bounds
     */
    public int getSunlight(int x, int y, int z) throws PosOutOfBoundsException {
        if (this.isOutOfBounds(x, y, z))
            return 0;

        return 15;
    }

    /**
     * Get the sunlight level at the given position.
     *
     * @param localBlockPos the position in the chunk
     * @return the sunlight level at the given position between 0 and 15
     */
    public int getSunlight(Vec3i localBlockPos) {
        return this.getSunlight(localBlockPos.x, localBlockPos.y, localBlockPos.z);
    }

    /**
     * Get the block light level at the given position.
     *
     * @param x the x position in the chunk
     * @param y the y position in the chunk
     * @param z the z position in the chunk
     * @return the block light level at the given position
     * @throws PosOutOfBoundsException if the position is out of bounds
     */
    public int getBlockLight(int x, int y, int z) throws PosOutOfBoundsException {
        if (this.isOutOfBounds(x, y, z))
            return 0;

        return this.lightMap.getBlockLight(x, y, z);
    }

    /**
     * Get the block light level at the given position.
     *
     * @param localBlockPos the position in the chunk
     * @return the block light level at the given position
     * @throws PosOutOfBoundsException if the position is out of bounds
     */
    public int getBlockLight(Vec3i localBlockPos) {
        return this.getBlockLight(localBlockPos.x, localBlockPos.y, localBlockPos.z);
    }

    /**
     * Get the brightness for the given light level.
     *
     * @param lightLevel the light level between 0 and 15
     * @return the brightness for the given light level between zero and one.
     */
    public float getBrightness(int lightLevel) {
        if (lightLevel > Chunk.MAX_LIGHT_LEVEL)
            return 1;
        if (lightLevel < 0)
            return 0;
        return Chunk.lightLevelMap[lightLevel];
    }

    protected void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity) {
        this.blockEntities.put(blockPos, blockEntity);
    }

    public Collection<BlockEntity> getBlockEntities() {
        return this.blockEntities.values();
    }

    public BlockEntity getBlockEntity(int x, int y, int z) {
        return this.blockEntities.get(new BlockPos(x, y, z));
    }

    public BlockEntity getBlockEntity(Vec3i localBlockPos) {
        return this.getBlockEntity(localBlockPos.x, localBlockPos.y, localBlockPos.z);
    }

    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return this.blockEntities.get(blockPos);
    }

    public void removeBlockEntity(BlockPos blockPos) {
        this.blockEntities.remove(blockPos);
    }

    /**
     * Chunk status for client chunk load response.
     *
     * @author <a href="https://github.com/XyperCode">XyperCode</a>
     */
    public enum Status {
        SUCCESS,
        SKIP,
        UNLOADED,
        FAILED
    }
}