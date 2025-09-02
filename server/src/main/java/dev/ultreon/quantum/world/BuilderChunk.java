package dev.ultreon.quantum.world;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.collection.PaletteStorage;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.InvalidThreadException;
import dev.ultreon.quantum.util.Point;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.gen.biome.BiomeGenerator;
import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.quantum.world.rng.RNG;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.ultreon.quantum.world.World.CS_2;

/**
 * The BuilderChunk class is an extension of the Chunk class,
 * specifically designed to handle chunk operations on a dedicated builder thread.
 * It includes functionality for handling biome data and block state manipulation.
 */
public final class BuilderChunk extends Chunk {
    private final @NotNull Thread thread;
    private final @NotNull Storage<@NotNull BiomeGenerator> biomeData;
    private @Nullable List<Vec3i> biomeCenters;
    private final @NotNull ServerWorld.Region region;
    private final @NotNull RNG rng;
    private final QuantumServer server;

    public BuilderChunk(@NotNull ServerWorld world, @NotNull Thread thread, ChunkVec pos, ServerWorld.@NotNull Region region) {
        super(world, pos);
        this.thread = thread;
        this.region = region;
        this.rng = new JavaRNG(this.world.getSeed() + (pos.getIntX() ^ ((long) pos.getIntZ() << 4)) & 0x3FFFFFFF);
        this.biomeData = new PaletteStorage<>(CS_2, world.getServer().getBiomes().plains.create((ServerWorld) this.world));
        this.server = world.getServer();
    }

    @Override
    protected void retrieveNeighbors() {
        for (Direction direction : Direction.values()) {
            Chunk chunk = ((ServerWorld) world).getChunkNoLoad(tmpCV.set(vec).add(direction.getOffset()));

            this.neighbors[direction.ordinal()] = chunk;
            if (chunk != null)
                chunk.neighbors[direction.getOpposite().ordinal()] = this;
        }
    }

    @Override
    public @NotNull BlockState getFast(int x, int y, int z) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        return super.getFast(x, y, z);
    }

    public boolean set(Point pos, BlockState block) {
        return set(pos.getIntX(), pos.getIntY(), pos.getIntZ(), block);
    }

    @Override
    public boolean set(int x, int y, int z, BlockState block) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        return super.set(x, y, z, block);
    }

    @Override
    protected void setFast(Vec3i pos, BlockState block) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        super.setFast(pos, block);
    }

    @Override
    protected boolean setFast(int x, int y, int z, BlockState block) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        return super.setFast(x, y, z, block);
    }

    @Override
    public @NotNull ServerWorld getWorld() {
        return (ServerWorld) this.world;
    }

    public boolean isOnInvalidThread() {
        if (GamePlatform.get().isWeb()) return false;
        return this.thread.getId() != Thread.currentThread().getId();
    }

    public boolean isOnBuilderThread() {
        if (GamePlatform.get().isWeb()) return true;
        return this.thread.getId() == Thread.currentThread().getId();
    }

    @SuppressWarnings("unchecked")
    public ServerChunk build() {
        Storage<RegistryKey<Biome>> map = this.biomeData.map(((ServerWorld) world).getServer().getBiomes().getDefaultKey(), RegistryKey[]::new, gen -> gen.getBiomeKey(((ServerWorld) world).getServer()));
        return new ServerChunk((ServerWorld) this.world, this.vec, this.storage, map, region);
    }

    public void setBiomeGenerator(int x, int z, BiomeGenerator generator) {
        int index = this.toFlatIndex(x, z);
        this.biomeData.set(index, generator);
    }

    public BiomeGenerator getBiomeGenerator(int x, int z) {
        int index = this.toFlatIndex(x, z);
        return this.biomeData.get(index);
    }

    public void setBiomeCenters(@Nullable List<Vec3i> biomeCenters) {
        this.biomeCenters = biomeCenters;
    }

    public @Nullable List<Vec3i> getBiomeCenters() {
        return this.biomeCenters;
    }

    public LightMap getLightMap() {
        return this.lightMap;
    }

    @Override
    public int getHeight(int x, int z) {
        return world.getHeight(x, z);
    }

    @Override
    public int getHeight(int x, int z, HeightmapType type) {
        return world.getHeight(x, z, type);
    }

    public RNG getRNG() {
        return this.rng;
    }

    public BuilderFork createFork(int x, int y, int z) {
        return new BuilderFork(this, x, y, z, ((ServerWorld) this.world).getGenerator());
    }

    public QuantumServer getServer() {
        return server;
    }
}
