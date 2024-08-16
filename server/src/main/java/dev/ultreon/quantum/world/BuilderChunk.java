package dev.ultreon.quantum.world;

import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.collection.PaletteStorage;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.util.InvalidThreadException;
import dev.ultreon.quantum.world.gen.biome.BiomeGenerator;
import dev.ultreon.quantum.world.gen.biome.Biomes;
import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.quantum.world.rng.RNG;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

public final class BuilderChunk extends Chunk {
    private final ServerWorld world;
    private final Thread thread;
    private final Storage<BiomeGenerator> biomeData;
    private List<Vec3i> biomeCenters;
    private final ServerWorld.Region region;
    private final RNG rng;

    public BuilderChunk(ServerWorld world, Thread thread, ChunkVec pos, ServerWorld.Region region) {
        super(world, pos);
        this.world = world;
        this.thread = thread;
        this.region = region;
        this.rng = new JavaRNG(this.world.getSeed() + (pos.getIntX() ^ ((long) pos.getIntZ() << 4)) & 0x3FFFFFFF);
        this.biomeData = new PaletteStorage<>(256, Biomes.PLAINS.create(this.world, world.getSeed()));
    }

    @Override
    public @NotNull BlockProperties getFast(int x, int y, int z) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        return super.getFast(x, y, z);
    }

    @Override
    public void set(Vec3i pos, BlockProperties block) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        if (this.isOutOfBounds(pos.x, pos.y, pos.z)) {
            this.world.recordOutOfBounds(this.offset.x + pos.x, this.offset.y + pos.y, this.offset.z + pos.z, block);
            return;
        }
        super.set(pos, block);
    }

    @Override
    public boolean set(int x, int y, int z, BlockProperties block) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        if (this.isOutOfBounds(x, y, z)) {
            this.world.recordOutOfBounds(this.offset.x + x, this.offset.y + y, this.offset.z + z, block);
            return false;
        }
        return super.set(x, y, z, block);
    }

    @Override
    public void setFast(Vec3i pos, BlockProperties block) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        if (this.isOutOfBounds(pos.x, pos.y, pos.z)) {
            this.world.recordOutOfBounds(this.offset.x + pos.x, this.offset.y + pos.y, this.offset.z + pos.z, block);
            return;
        }
        super.setFast(pos, block);
    }

    @Override
    public boolean setFast(int x, int y, int z, BlockProperties block) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        if (this.isOutOfBounds(x, y, z)) {
            this.world.recordOutOfBounds(this.offset.x + x, this.offset.y + y, this.offset.z + z, block);
            return false;
        }
        return super.setFast(x, y, z, block);
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }

    public boolean isOnInvalidThread() {
        return this.thread.threadId() != Thread.currentThread().threadId();
    }

    public boolean isOnBuilderThread() {
        return this.thread.threadId() == Thread.currentThread().threadId();
    }

    public ServerChunk build() {
        Storage<Biome> map = this.biomeData.map(Biomes.PLAINS, Biome.class, BiomeGenerator::getBiome);
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

    public void setBiomeCenters(List<Vec3i> biomeCenters) {
        this.biomeCenters = biomeCenters;
    }

    public List<Vec3i> getBiomeCenters() {
        return this.biomeCenters;
    }

    public LightMap getLightMap() {
        return this.lightMap;
    }

    @Override
    public int getHeight(int x, int z) {
        if (isOutOfBounds(x, 0, z)) {
            // Get from world
            int globX = this.getVec().x * CHUNK_SIZE + x;
            int globZ = this.getVec().z * CHUNK_SIZE + z;
            return this.world.getHeight(globX, globZ);
        }
        return super.getHeight(x, z, HeightmapType.WORLD_SURFACE);
    }

    @Override
    public int getHeight(int x, int z, HeightmapType type) {
        if (isOutOfBounds(x, 0, z)) {
            // Get from world
            int globX = this.getVec().x * CHUNK_SIZE + x;
            int globZ = this.getVec().z * CHUNK_SIZE + z;
            return this.world.getHeight(globX, globZ, type);
        }
        return super.getHeight(x, z, type);
    }

    public RNG getRNG() {
        return this.rng;
    }
}
