package dev.ultreon.quantum.world.gen.biome;

import com.badlogic.gdx.utils.Disposable;
import com.google.common.base.Preconditions;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.debug.WorldGenDebugContext;
import dev.ultreon.quantum.util.BlockMetaPredicate;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.gen.RecordingChunk;
import dev.ultreon.quantum.world.gen.TreeData;
import dev.ultreon.quantum.world.gen.TreeGenerator;
import dev.ultreon.quantum.world.gen.WorldGenFeature;
import dev.ultreon.quantum.world.gen.layer.TerrainLayer;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.rng.RNG;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;

import static dev.ultreon.quantum.world.World.CHUNK_HEIGHT;

public class BiomeGenerator implements Disposable {
    private final World world;
    private final List<TerrainLayer> layers;
    private final List<WorldGenFeature> features;
    public static final boolean USE_DOMAIN_WARPING = true;
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

    public BuilderChunk processColumn(BuilderChunk chunk, int x, int y, int z, Collection<ServerWorld.RecordedChange> recordedChanges) {
        LightMap lightMap = chunk.getLightMap();

        this.generateTerrainLayers(chunk, x, z, y);

        BiomeGenerator.setRecordedChanges(chunk, x, z, recordedChanges);

        BiomeGenerator.updateLightMap(chunk, x, z, lightMap);
        chunk.set(x, chunk.getOffset().y, z, Blocks.VOIDGUARD.createMeta());

        return chunk;
    }

    private static void updateLightMap(BuilderChunk chunk, int x, int z, LightMap lightMap) {
        int highest = chunk.getHighest(x, z, BlockMetaPredicate.WG_HEIGHT_CHK);
        for (int y = chunk.getOffset().y; y < chunk.getOffset().y + CHUNK_HEIGHT; y++) {
            lightMap.setSunlight(x, y, z, y >= highest ? 15 : 7);
        }
    }

    public void generateTerrainFeatures(RecordingChunk chunk, int x, int z, int groundPos) {
        for (var feature : this.features) {
            feature.handle(this.world, chunk, x, z, groundPos);
        }
    }

    private void generateTerrainLayers(BuilderChunk chunk, int x, int z, int groundPos) {
        RNG rng = chunk.getRNG();
        for (int y = chunk.getOffset().y + 1; y < chunk.getOffset().y + CHUNK_HEIGHT; y++) {
            if (chunk.get(x, y, z).isAir()) continue;

            for (var layer : this.layers) {
                if (layer.handle(this.world, chunk, rng, x, y, z, groundPos)) {
                    break;
                }
            }
        }
    }

    private static void setRecordedChanges(BuilderChunk chunk, int x, int z, Collection<ServerWorld.RecordedChange> recordedChanges) {
        for (ServerWorld.RecordedChange recordedChange : recordedChanges) {
            boolean isWithinChunkBounds = recordedChange.x() >= chunk.getOffset().x && recordedChange.x() < chunk.getOffset().x + World.CHUNK_SIZE
                    && recordedChange.z() >= chunk.getOffset().z && recordedChange.z() < chunk.getOffset().z + World.CHUNK_SIZE;
            BlockVec localBlockVec = World.toLocalBlockVec(recordedChange.x(), recordedChange.y(), recordedChange.z());
            if (isWithinChunkBounds && localBlockVec.x() == x && localBlockVec.z() == z) {
                chunk.set(World.toLocalBlockVec(recordedChange.x(), recordedChange.y(), recordedChange.z()).vec(), recordedChange.block());
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
