package dev.ultreon.quantum.world.gen.chunk;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.HeightmapType;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.gen.carver.Carver;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.gen.noise.NoiseConfigs;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

/**
 * An abstract class implementing the ChunkGenerator interface, providing a base
 * for generating chunks in a world using biomes and domain warping layers.
 */
public abstract class SimpleChunkGenerator implements ChunkGenerator {
    private final Registry<Biome> biomesRegistry;
    private final Array<Biome> biomes = new Array<>();
    private DomainWarping layerDomain;

    /**
     * Constructs a SimpleChunkGenerator with a registry of biomes.
     *
     * @param biomeRegistry The Registry instance containing biome information. Must not be null.
     */
    public SimpleChunkGenerator(Registry<Biome> biomeRegistry) {
        biomesRegistry = biomeRegistry;
    }

    @Override
    public void create(@NotNull ServerWorld world, long seed) {
        NoiseConfigs noiseConfigs = world.getServer().getNoiseConfigs();
        layerDomain = new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed)));
    }

    @Override
    public void generate(@NotNull ServerWorld world, BuilderChunk chunk, Collection<ServerWorld.@NotNull RecordedChange> changes) {
        Carver carver = getCarver();

        this.generateTerrain(chunk, carver);
        world.getFeatureData().prepareChunk(chunk);

        this.generateFeatures(chunk);
        this.generateStructures(chunk);
    }

    @Override
    public DomainWarping getLayerDomain() {
        return layerDomain;
    }

    /**
     * Generates terrain for a given chunk using a specified carver and records the changes.
     *
     * @param chunk The chunk in which the terrain generation is to be performed. Must not be null.
     * @param carver The carver used to shape the terrain within the chunk. Must not be null.
     */
    protected abstract void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver);

    /**
     * Generates terrain features within a specified chunk based on biome and recording information.
     *
     * @param chunk The chunk for which the terrain features are being generated. Must not be null.
     */
    protected void generateFeatures(BuilderChunk chunk) {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int height = chunk.getWorld().getHeight(chunk.getOffset().x + x, chunk.getOffset().z + z, HeightmapType.WORLD_SURFACE);
                chunk.getBiomeGenerator(x, z).generateTerrainFeatures(chunk, x, z, height);
            }
        }
    }

    /**
     * Generates various structures within the provided chunk by leveraging the biome generator
     * for the chunk's regions.
     * It iterates through the X and Z coordinates of the chunk, determines
     * the highest Y coordinate at each (X, Z) pair, and triggers the biome-specific structure
     * generation at those points.
     *
     * @param chunk The BuilderChunk instance where the structures will be generated. Must not be null.
     */
    protected void generateStructures(BuilderChunk chunk) {
        for (var x = 0; x < CHUNK_SIZE; x++) {
            for (var z = 0; z < CHUNK_SIZE; z++) {
                chunk.getBiomeGenerator(x, z).generateStructureFeatures(chunk);
            }
        }
    }

    /**
     * Adds a biome to the list of biomes managed by this chunk generator.
     *
     * @param biome The registry key of the biome to be added. Must not be null.
     */
    protected final void addBiome(RegistryKey<Biome> biome) {
        biomes.add(biomesRegistry.get(biome));
    }

    /**
     * Provides the Carver instance responsible for shaping and carving terrain within a chunk.
     *
     * @return An instance of Carver which is used for terrain generation. Must not be null.
     */
    public abstract @NotNull Carver getCarver();

    @Override
    public void dispose() {

    }
}
