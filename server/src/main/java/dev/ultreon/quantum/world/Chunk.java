package dev.ultreon.quantum.world;

import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.collection.PaletteStorage;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.ChunkVecSpace;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

import static dev.ultreon.libs.commons.v0.Mth.lerp;
import static dev.ultreon.quantum.world.World.*;

/**
 * Represents a chunk in the world.
 * <p style="color: red;">NOTE: This class isn't meant to be extended</p>
 * <p style="color: red;">NOTE: This class isn't thread safe</p>
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
@NotThreadSafe
@ApiStatus.NonExtendable
public abstract class Chunk extends GameObject implements Disposable, ChunkAccess {
    public static final int VERTEX_SIZE = 6;
    protected static final int MAX_LIGHT_LEVEL = 15;
    protected static final float[] lightLevelMap = new float[Chunk.MAX_LIGHT_LEVEL + 1];

    public final @NotNull ChunkVec vec;
    final @NotNull Map<BlockVec, Float> breaking = new HashMap<>();
    protected boolean active;
    protected boolean ready;

    protected final @NotNull BlockVec offset;

    private boolean disposed;
    protected final @NotNull World world;

    public final @NotNull Storage<BlockState> storage;
    protected final @NotNull LightMap lightMap = new LightMap(CS_3);
    public final @NotNull Storage<RegistryKey<Biome>> biomeStorage;

    static {
        for (int i = 0; i <= Chunk.MAX_LIGHT_LEVEL; i++) {
            double lerp = lerp(0.15f, 1.5f, (double) i / Chunk.MAX_LIGHT_LEVEL);
            Chunk.lightLevelMap[i] = (float) lerp;
        }
    }

    private final Map<BlockVec, BlockEntity> blockEntities = new HashMap<>();

    /**
     * @deprecated Use {@link #Chunk(World, ChunkVec)} instead@
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    protected Chunk(@NotNull World world,
                    int size,
                    int height,
                    @NotNull ChunkVec vec) {
        this(world, size, height, vec, new PaletteStorage<>(size * height * size, Blocks.AIR.getDefaultState()));
    }

    /**
     * @deprecated Use {@link #Chunk(World, ChunkVec, Storage)} instead
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    protected Chunk(@NotNull World world,
                    int size,
                    int height,
                    @NotNull ChunkVec vec,
                    @NotNull Storage<BlockState> storage) {
        this(world, size, height, vec, storage, new PaletteStorage<>(CS_2, RegistryKey.of(RegistryKeys.BIOME, new NamespaceID("plains"))));
    }

    /**
     * @deprecated Use {@link #Chunk(World, ChunkVec, Storage, Storage)} instead@
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    protected Chunk(@NotNull World world,
                    int ignoredSize,
                    int ignoredHeight,
                    @NotNull ChunkVec vec,
                    @NotNull Storage<BlockState> storage,
                    @NotNull Storage<RegistryKey<Biome>> biomeStorage) {
        this(world, vec, storage, biomeStorage);
    }

    /**
     * Creates a new chunk.
     *
     * @param world the world the chunk is in.
     * @param vec   the chunk position in world space.
     *
     * @throws IllegalArgumentException if the chunk vector is not in world space.
     */
    protected Chunk(@NotNull World world,
                    @NotNull ChunkVec vec) {
        this(world, vec, new PaletteStorage<>(CS_3, Blocks.AIR.getDefaultState()));
    }

    /**
     * Creates a new chunk.
     *
     * @param world   the world the chunk is in.
     * @param vec     the chunk position in world space.
     * @param storage the block storage.
     *
     * @throws IllegalArgumentException if the chunk vector is not in world space.
     */
    protected Chunk(@NotNull World world,
                    @NotNull ChunkVec vec,
                    @NotNull Storage<BlockState> storage) {
        this(world, vec, storage, new PaletteStorage<>(CS_2, RegistryKey.of(RegistryKeys.BIOME, new NamespaceID("plains"))));
    }

    /**
     * Creates a new chunk.
     *
     * @param world        the world the chunk is in.
     * @param vec          the chunk position in world space.
     * @param storage      the block storage.
     * @param biomeStorage the biome storage.
     *
     * @throws IllegalArgumentException if the chunk vector is not in world space.
     */
    protected Chunk(@NotNull World world,
                    @NotNull ChunkVec vec,
                    @NotNull Storage<BlockState> storage,
                    @NotNull Storage<RegistryKey<Biome>> biomeStorage) {
        this.world = world;

        if (vec.getSpace() != ChunkVecSpace.WORLD)
            throw new IllegalArgumentException("ChunkVec must be in world space");

        this.offset = new BlockVec(vec.getIntX() * CS, vec.getIntY() * CS, vec.getIntZ() * CS, BlockVecSpace.WORLD);

        this.vec = vec;
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
        return Registries.BLOCK.byRawId(id);
    }

    /**
     * Decodes a block from a UBO object.
     *
     * @param data The input data.
     * @return The decoded block data.
     * @deprecated Use {@link BlockState#load(MapType)} instead
     */
    @Deprecated
    public static BlockState loadBlock(MapType data) {
        return BlockState.load(data);
    }

    /**
     * Gets the block at the given coordinates. Note that this method is not thread safe.
     *
     * @param vec the position of the block
     * @return the block at the given coordinates
     */
    @Override
    public @NotNull BlockState get(Point vec) {
        synchronized (this) {
            if (this.disposed) return Blocks.AIR.getDefaultState();
            return this.get(vec.getIntX(), vec.getIntY(), vec.getIntZ());
        }
    }

    /**
     * Gets the block at the given coordinates. Note that this method is not thread safe.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @return the block at the given coordinates
     */
    @Override
    public @NotNull BlockState get(int x, int y, int z) {
        synchronized (this) {
            if (this.isOutOfBounds(x, y, z))
                throw new IllegalArgumentException("Coordinates out of bounds: " + x + ", " + y + ", " + z);
            if (this.disposed) return Blocks.AIR.getDefaultState();
            return this.getFast(x, y, z);
        }
    }

    /**
     * Gets the block at the given coordinates.
     * Note that this method is not thread safe.
     * <p>This method isn't checking for out of bounds, so be careful when using it.</p>
     *
     * @param pos the position of the block
     * @return the block at the given coordinates
     */
    protected BlockState getFast(Vec3i pos) {
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
    @NotNull
    protected BlockState getFast(int x, int y, int z) {
        synchronized (this) {
            BlockState state = Blocks.AIR.getDefaultState();
            if (this.disposed) return state;
            int dataIdx = this.getIndex(x, y, z);

            BlockState block = this.storage.get(dataIdx);
            return block == null ? state : block;
        }
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
    @Override
    public boolean set(int x, int y, int z, BlockState block) {
        if (this.isOutOfBounds(x, y, z))
            throw new PosOutOfBoundsException("Position " + x + ", " + y + ", " + z + " is out of bounds for chunk.");
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
    protected void setFast(Vec3i pos, BlockState block) {
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
    protected boolean setFast(int x, int y, int z, BlockState block) {
         if (this.disposed) return false;
        synchronized (this){
            int index = this.getIndex(x, y, z);

            this.breaking.remove(new BlockVec(x, y, z, BlockVecSpace.CHUNK));
            this.storage.set(index, block);

//            // Update the world surface heightmap
//            Heightmap worldSurfaceHeightmap = this.world.heightMapAt(vec, HeightmapType.WORLD_SURFACE);
//
//            int oldSurfaceHeight = worldSurfaceHeightmap.get(x, z);
//            if (y > oldSurfaceHeight && !block.isAir()) worldSurfaceHeightmap.set(x, z, (short) y);
//            else if (y < oldSurfaceHeight && block.isAir()) for (int cy = oldSurfaceHeight; cy > y; cy--) {
//                if (getFast(x, cy, z).isAir()) continue;
//                worldSurfaceHeightmap.set(x, z, (short) cy);
//                break;
//            }
//
//            // Update the motion blocking heightmap
//            Heightmap motionBlockingHeightmap = this.world.heightMapAt(vec, HeightmapType.MOTION_BLOCKING);
//
//            int oldColliderHeight = motionBlockingHeightmap.get(x, z);
//            if (y > oldColliderHeight && !block.hasCollider()) motionBlockingHeightmap.set(x, z, (short) y);
//            else if (y < oldColliderHeight && block.hasCollider()) for (int cy = oldColliderHeight; cy > y; cy--) {
//                if (getFast(x, cy, z).hasCollider()) continue;
//                motionBlockingHeightmap.set(x, z, (short) cy);
//                break;
//            }
        }

        return true;
    }

    /**
     * Calculates the index of the block at the given coordinates in the chunk array.
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @return the index of the block in the chunk array, or -1 if the block is out of bounds
     */
    private int getIndex(int x, int y, int z) {
        if (x >= 0 && x < CS && y >= 0 && y < CS && z >= 0 && z < CS) {
            return z * (CS_2) + y * CS + x;
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
    public boolean isOutOfBounds(int x, int y, int z) {
        return x < 0 || x >= CS || y < 0 || y >= CS || z < 0 || z >= CS;
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
        synchronized (this) {
            if (this.disposed) return;
            this.disposed = true;
            this.ready = false;
            this.breaking.clear();
            this.blockEntities.clear();
        }
    }

    @Override
    public String toString() {
        return "Chunk[x=" + this.getVec().getIntX() + ", y=" + this.getVec().getIntY() + ", z=" + this.getVec().getIntZ() + "]";
    }

    @Override
    public BlockVec getOffset() {
        return this.offset.cpy();
    }

    float getBreakProgress(float x, float y, float z) {
        BlockVec pos = new BlockVec((int) x, (int) y, (int) z, BlockVecSpace.CHUNK);
        Float v = this.breaking.get(pos);
        if (v != null) {
            return v;
        }
        return -1.0F;
    }

    public void startBreaking(int x, int y, int z) {
        this.breaking.put(new BlockVec(x, y, z, BlockVecSpace.CHUNK), 0.0F);
    }

    public boolean stopBreaking(int x, int y, int z) {
        Float remove = this.breaking.remove(new BlockVec(x, y, z, BlockVecSpace.CHUNK));
        return remove != null && remove > 0.5F;
    }

    public BreakResult continueBreaking(int x, int y, int z, float amount) {
        BlockVec pos = new BlockVec(x, y, z, BlockVecSpace.CHUNK);
        if (!this.breaking.containsKey(pos)) return BreakResult.FAILED;
        Float v = this.breaking.computeIfPresent(pos, (pos1, cur) -> Mth.clamp(cur + amount, 0, 1));
        if (v != null && v == 1.0F) {
            this.breaking.remove(pos);
            return BreakResult.BROKEN;
        }
        return BreakResult.CONTINUE;
    }

    public Map<BlockVec, Float> getBreaking() {
        return Collections.unmodifiableMap(this.breaking);
    }

    public boolean isReady() {
        return this.ready;
    }

    @Override
    public boolean isDisposed() {
        return this.disposed;
    }

    @ApiStatus.Internal
    public void onUpdated() {
        this.world.onChunkUpdated(this);
    }

    @Override
    public @NotNull World getWorld() {
        return this.world;
    }

    @Override
    public void reset() {
        // No-op
    }

    public boolean isActive() {
        return this.active;
    }

    /**
     * @deprecated use {@link #vec} instead.
     */
    @Deprecated
    public @NotNull ChunkVec getVec() {
        return this.vec;
    }

    /**
     * Find the highest block at the given position.
     *
     * @param x the x coordinate
     * @param z the z coordinate
     * @return The highest block Y coordinate.
     */
    @Override
    public int getHeight(int x, int z) {
        return this.world.getHeight(this.offset.x + x, this.offset.z + z);
    }

    /**
     * Find the highest block at the given position.
     *
     * @param x the x coordinate
     * @param z the z coordinate
     * @return The highest block Y coordinate.
     */
    public int getHeight(int x, int z, HeightmapType type) {
        return this.world.getHeight(this.offset.x + x, this.offset.z + z, type);
    }

    /**
     * Find an empty space above the given position.
     *
     * @param x The x position.
     * @param y The y position to use as base.
     * @param z The z position.
     * @return The found position.
     */
    public @Nullable Integer ascend(int x, int y, int z) {
        if (x < 0 || x >= CS || y < 0 || y >= CS || z < 0 || z >= CS)
            return null;

        for (; y < y + 256; y++) {
            if (this.getFast(x, y, z).isAir()) {
                return y;
            }
        }
        return null;
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
    public @Nullable Integer ascend(int x, int y, int z, int height) {
        if (x < 0 || x >= CS || y < 0 || y >= CS || z < 0 || z >= CS)
            return null;

        for (; y < y + height; y++) {
            if (!this.getFast(x, y, z).isAir()) continue;

            for (int i = 0; i < height; i++) {
                if (this.getFast(x, y + i, z).isAir()) return y;
            }
        }
        return null;
    }

    protected int toFlatIndex(int x, int z) {
        return x + z * CS;
    }

    /**
     * Get the biome at the given position.
     *
     * @param x        the x position in the chunk
     * @param ignoredY the y position in the chunk
     * @param z        the z position in the chunk
     * @return the biome at the given position
     * @deprecated deprecated until biomes are 3 dimensional
     */
    @Deprecated
    public RegistryKey<Biome> getBiome(int x, int ignoredY, int z) {
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
    public RegistryKey<Biome> getBiome(int x, int z) {
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
        return Objects.equals(this.vec, chunk.vec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.vec);
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

        return lightMap.getSunlight(x, y, z);
    }

    public BlockState getSafe(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= CS || y >= CS || z >= CS) {
            BlockVec start = vec.start();
            x += start.x;
            y += start.y;
            z += start.z;

            return world.get(x, y, z);
        }

        return get(x, y, z);
    }

    public boolean setSafe(int x, int y, int z, BlockState state) {
        if (x < 0 || y < 0 || z < 0 || x >= CS || y >= CS || z >= CS) {
            BlockVec start = vec.start();
            x += start.x;
            y += start.y;
            z += start.z;

            return world.set(x, y, z, state);
        }

        return set(x, y, z, state);
    }

    /**
     * Get the sunlight level at the given position.
     *
     * @param localBlockVec the position in the chunk
     * @return the sunlight level at the given position between 0 and 15
     */
    public int getSunlight(Vec3i localBlockVec) {
        return this.getSunlight(localBlockVec.x, localBlockVec.y, localBlockVec.z);
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
     * @param localBlockVec the position in the chunk
     * @return the block light level at the given position
     * @throws PosOutOfBoundsException if the position is out of bounds
     */
    public int getBlockLight(Vec3i localBlockVec) {
        return this.getBlockLight(localBlockVec.x, localBlockVec.y, localBlockVec.z);
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

    protected void setBlockEntity(BlockVec blockVec, BlockEntity blockEntity) {
        synchronized (this) {
            this.blockEntities.put(blockVec, blockEntity);
        }
    }

    public Collection<BlockEntity> getBlockEntities() {
        return this.blockEntities.values();
    }

    public BlockEntity getBlockEntity(int x, int y, int z) {
        return this.blockEntities.get(new BlockVec(x, y, z, BlockVecSpace.CHUNK));
    }

    public BlockEntity getBlockEntity(Vec3i localBlockVec) {
        return this.getBlockEntity(localBlockVec.x, localBlockVec.y, localBlockVec.z);
    }

    public BlockEntity getBlockEntity(BlockVec blockVec) {
        return this.blockEntities.get(blockVec);
    }

    public void removeBlockEntity(BlockVec blockVec) {
        this.blockEntities.remove(blockVec);
    }

    public boolean isUniform() {
        return storage.isUniform();
    }

    /**
     * Chunk status for client chunk load response.
     *
     * @author <a href="https://github.com/XyperCode">Qubilux</a>
     */
    public enum Status {
        SUCCESS,
        UNLOADED,
        FAILED
    }
}