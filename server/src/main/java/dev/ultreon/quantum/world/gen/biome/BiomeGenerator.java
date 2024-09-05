package dev.ultreon.quantum.world.gen.biome;

import com.badlogic.gdx.utils.Disposable;
import com.google.common.base.Preconditions;
import dev.ultreon.quantum.debug.WorldGenDebugContext;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.gen.TreeData;
import dev.ultreon.quantum.world.gen.TreeGenerator;
import dev.ultreon.quantum.world.gen.WorldGenFeature;
import dev.ultreon.quantum.world.gen.chunk.RecordingChunk;
import dev.ultreon.quantum.world.gen.layer.TerrainLayer;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.rng.RNG;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

public class BiomeGenerator implements Disposable {
    private final World world;
    private final List<TerrainLayer> layers;
    private final List<WorldGenFeature> features;
    @UnknownNullability
    public TreeGenerator treeGenerator;
    private final Biome biome;

    public BiomeGenerator(World world, Biome biome, DomainWarping domainWarping, List<TerrainLayer> layers, List<WorldGenFeature> features) {
        Preconditions.checkNotNull(biome, "biome");
        this.world = world;
        this.biome = biome;
        this.layers = layers;
        this.features = features;
    }

    public void processColumn(BuilderChunk chunk, int x, int y, int z, Collection<ServerWorld.RecordedChange> recordedChanges) {
//        LightMap lightMap = chunk.getLightMap();

        this.generateTerrainLayers(chunk, x, z, y);

        BiomeGenerator.setRecordedChanges(chunk, x, chunk.getOffset().y, z, recordedChanges);

//        BiomeGenerator.updateLightMap(chunk, x, z, lightMap); // TODO
    }

//    private static void updateLightMap(BuilderChunk chunk, int x, int z, LightMap lightMap) {
//        int highest = chunk.getHeight(x, z, HeightmapType.WORLD_SURFACE);
//        for (int y = chunk.getOffset().y; y < chunk.getOffset().y + CHUNK_HEIGHT; y++) {
//            lightMap.setSunlight(x, y, z, y >= highest ? 15 : 7);
//        }
//    }

    public void generateTerrainFeatures(RecordingChunk chunk, int x, int z, int groundPos) {
        for (int y = 0; y < CHUNK_SIZE; y++) {
            for (var feature : this.features) {
                if (feature.handle(this.world, chunk, x, y, z, groundPos)) {
                    break;
                }
            }
        }
    }

    private void generateTerrainLayers(BuilderChunk chunk, int x, int z, int groundPos) {
        RNG rng = chunk.getRNG();
        if (chunk.getVec().y > 256 / CHUNK_SIZE)
            return;

        BlockVec offset = chunk.getOffset();
        for (int y = 0; y < CHUNK_SIZE; y++) {
            if (chunk.get(x, y, z).isAir()) continue;

            for (var layer : this.layers) {
                if (layer.handle(this.world, chunk, rng, offset.x + x, offset.y + y, offset.z + z, groundPos)) {
                    break;
                }
            }
        }
    }

    private static void setRecordedChanges(BuilderChunk chunk, int x, int y, int z, Collection<ServerWorld.RecordedChange> recordedChanges) {
        for (ServerWorld.RecordedChange recordedChange : recordedChanges) {
            boolean isWithinChunkBounds = recordedChange.x() >= chunk.getOffset().x && recordedChange.x() < chunk.getOffset().x + CHUNK_SIZE
                                          && recordedChange.y() >= chunk.getOffset().y && recordedChange.y() < chunk.getOffset().y + CHUNK_SIZE
                                          && recordedChange.z() >= chunk.getOffset().z && recordedChange.z() < chunk.getOffset().z + CHUNK_SIZE;
            BlockVec localBlockVec = World.toLocalBlockVec(recordedChange.x(), recordedChange.y(), recordedChange.z());
            if (isWithinChunkBounds && localBlockVec.getIntX() == x && localBlockVec.getIntY() == y && localBlockVec.getIntZ() == z) {
                chunk.set(new BlockVec(recordedChange.x(), recordedChange.y(), recordedChange.z(), BlockVecSpace.WORLD).chunkLocal().vec(), recordedChange.block());
                if (WorldGenDebugContext.isActive()) {
                    System.out.println("[DEBUG CHUNK-HASH " + System.identityHashCode(chunk) + "] Setting recorded change in chunk at " + recordedChange.x() + ", " + recordedChange.y() + ", " + recordedChange.z() + " of type " + recordedChange.block());
                }
            }
        }
    }

    public TreeData createTreeData(Chunk chunk) {
        if (this.treeGenerator == null)
            return new TreeData();

        return this.treeGenerator.generateTreeData(chunk);
    }

    @Override
    public void dispose() {
        this.layers.forEach(TerrainLayer::dispose);
        this.features.forEach(WorldGenFeature::dispose);
    }

    public World getWorld() {
        return this.world;
    }

    public Biome getBiome() {
        return this.biome;
    }

    public RegistryKey<Biome> getBiomeKey(QuantumServer server) {
        return server.getRegistries().get(RegistryKeys.BIOME).getKey(this.biome);
    }

    public void generateStructureFeatures(RecordingChunk recordingChunk, int x, int y, int z) {

    }

    public static class Index {
        public BiomeGenerator biomeGenerator;
        @Nullable
        public Integer terrainSurfaceNoise;

        public Index(BiomeGenerator biomeGenerator) {
            this(biomeGenerator, null);
        }

        public Index(BiomeGenerator biomeGenerator, @Nullable Integer terrainSurfaceNoise) {
            this.biomeGenerator = biomeGenerator;
            this.terrainSurfaceNoise = terrainSurfaceNoise;
        }
    }
}
