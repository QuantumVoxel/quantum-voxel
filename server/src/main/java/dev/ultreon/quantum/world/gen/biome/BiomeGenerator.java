package dev.ultreon.quantum.world.gen.biome;

import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.gen.FeatureData;
import dev.ultreon.quantum.world.gen.FeatureInfo;
import dev.ultreon.quantum.world.gen.StructureInstance;
import dev.ultreon.quantum.world.gen.TerrainFeature;
import dev.ultreon.quantum.world.gen.layer.TerrainLayer;
import dev.ultreon.quantum.world.rng.RNG;
import dev.ultreon.quantum.world.structure.BlockPoint;
import dev.ultreon.quantum.world.structure.WorldSlice;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static dev.ultreon.quantum.world.World.CS;

/**
 * The BiomeGenerator class is responsible for generating terrain and features for a given biome in the world.
 * It applies various terrain layers and world generation features to specific chunk columns.
 */
public class BiomeGenerator implements Disposable {
    private final World world;
    private final List<TerrainLayer> layers;
    private final List<TerrainFeature> surfaceFeatures;
    private final List<TerrainFeature> undergroundFeatures;
    private final Biome biome;

    /**
     * Constructs a BiomeGenerator with the specified parameters.
     *
     * @param world the world in which the biome generator will operate
     * @param biome the biome type to be generated
     * @param layers the list of terrain layers to apply in the world generation
     * @param surfaceFeatures the list of world generation surface features to include
     */
    public BiomeGenerator(World world, Biome biome, List<TerrainLayer> layers, List<TerrainFeature> surfaceFeatures, List<TerrainFeature> undergroundFeatures) {        this.world = world;
        this.biome = biome;
        this.layers = layers;
        this.surfaceFeatures = surfaceFeatures;
        this.undergroundFeatures = undergroundFeatures;
    }

    public void processColumn(BuilderChunk chunk, int x, int y, int z) {
//        LightMap lightMap = chunk.getLightMap();

        this.generateTerrainLayers(chunk, x, z, y);
    }

    /**
     * Generates terrain features for a specified chunk column by applying various world generation features.
     *
     * @param chunk the chunk in which to generate the terrain features
     * @param x the x-coordinate within the chunk
     * @param z the z-coordinate within the chunk
     * @param groundPos the ground position at the specified coordinates
     */
    public void generateTerrainFeatures(BuilderChunk chunk, int x, int z, int groundPos) {
        for (int y = 0; y < CS; y++) {
            BlockVec blockInWorld = chunk.vec.blockInWorldSpace(x, y, z);

            for (var feature : this.undergroundFeatures) {
                if (genFeature(chunk, blockInWorld.x, blockInWorld.y, blockInWorld.z, feature)) break;
            }
        }

        for (var feature : this.surfaceFeatures) {
            BlockVec blockInWorld = chunk.vec.blockInWorldSpace(x, 0, z);

            if (genFeature(chunk, blockInWorld.x, groundPos, blockInWorld.z, feature)) break;
        }
    }

    public boolean genFeature(BuilderChunk chunk, int x, int y, int z, TerrainFeature feature) {
        BuilderFork fork = chunk.createFork(x, y, z);
        RNG rng = chunk.getRNG();
        long posSeed = rng.nextLong();
        BlockVec localVec = new BlockVec(x, y, z, BlockVecSpace.WORLD).chunkLocal();
        if (feature.shouldPlace(x, y, z, chunk.get(localVec.x, localVec.y, localVec.z))) {
            if (feature.handle(fork, posSeed, 0, 0, 0)) {
                List<BlockPoint> points = fork.getPositions();
                ServerWorld serverWorld = chunk.getWorld();
                FeatureData featureData = serverWorld.getFeatureData();
                featureData.writeFeature(chunk, new FeatureInfo(points));
                return true;
            }
        }
        return false;
    }

    /**
     * Generates terrain layers for a specified chunk column by applying various terrain layers.
     *
     * @param chunk the chunk in which to generate the terrain layers
     * @param x the x-coordinate within the chunk
     * @param z the z-coordinate within the chunk
     * @param groundPos the ground position at the specified coordinates
     */
    public void generateTerrainLayers(BuilderChunk chunk, int x, int z, int groundPos) {
        RNG rng = chunk.getRNG();
        if (chunk.getVec().y > 256 / CS)
            return;

        BlockVec offset = chunk.getOffset();
        for (int y = 0; y < CS; y++) {
            if (chunk.get(x, y, z).isAir()) continue;

            for (var layer : this.layers) {
                if (layer.handle(this.world, new WorldSlice(chunk), rng, offset.x + x, offset.y + y, offset.z + z, groundPos)) {
                    break;
                }
            }
        }
    }

    public void generateStructureFeatures(BuilderChunk recordingChunk) {
        Collection<StructureInstance> structures = recordingChunk.getWorld().getStructuresAt(recordingChunk.getVec());

        for (StructureInstance struc : structures) {
            struc.placeSlice(recordingChunk);
        }
    }

    @Override
    public void dispose() {
        this.layers.forEach(TerrainLayer::dispose);
        this.surfaceFeatures.forEach(TerrainFeature::dispose);
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
