package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.collection.PaletteStorage;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.util.InvalidThreadException;
import dev.ultreon.quantum.util.Point;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.gen.biome.BiomeGenerator;
import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.quantum.world.rng.RNG;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

/**
 * The BuilderChunk class is an extension of the Chunk class,
 * specifically designed to handle chunk operations on a dedicated builder thread.
 * It includes functionality for handling biome data and block state manipulation.
 */
public final class BuilderChunk extends Chunk {
    private final @NotNull ServerWorld world;
    private final @NotNull Thread thread;
    private final @NotNull Storage<BiomeGenerator> biomeData;
    private @Nullable List<Vec3i> biomeCenters;
    private final @NotNull ServerWorld.Region region;
    private final @NotNull RNG rng;

    public BuilderChunk(@NotNull ServerWorld world, @NotNull Thread thread, ChunkVec pos, ServerWorld.@NotNull Region region) {
        super(world, pos);
        this.world = world;
        this.thread = thread;
        this.region = region;
        this.rng = new JavaRNG(this.world.getSeed() + (pos.getIntX() ^ ((long) pos.getIntZ() << 4)) & 0x3FFFFFFF);
        this.biomeData = new PaletteStorage<>(CHUNK_SIZE * CHUNK_SIZE, world.getServer().getBiomes().plains.create(this.world, world.getSeed()));
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
        return this.world;
    }

    public boolean isOnInvalidThread() {
        return this.thread.threadId() != Thread.currentThread().threadId();
    }

    public boolean isOnBuilderThread() {
        return this.thread.threadId() == Thread.currentThread().threadId();
    }

    @SuppressWarnings("unchecked")
    public ServerChunk build() {
        Storage<RegistryKey<Biome>> map = this.biomeData.map(world.getServer().getBiomes().getDefaultKey(), RegistryKey[]::new, gen -> gen.getBiomeKey(world.getServer()));
        return new ServerChunk(this.world, this.getVec(), this.storage, map, region);
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
    @Deprecated
    public int getHeight(int x, int z) {
        return world.getHeight(x, z);
    }

    @Override
    @Deprecated
    public int getHeight(int x, int z, HeightmapType type) {
        return world.getHeight(x, z, type);
    }

    public RNG getRNG() {
        return this.rng;
    }

    public Stream<Vec3d> getCavePoints() {
        return this.world.getCavePointsFor(getVec());
    }

    public BuilderFork createFork(int x, int y, int z) {
        return new BuilderFork(this, x, y, z, this.world.getGenerator());
    }

}
