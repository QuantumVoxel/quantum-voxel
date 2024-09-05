package dev.ultreon.quantum.world.gen.chunk;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.debug.WorldGenDebugContext;
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

public abstract class SimpleChunkGenerator implements ChunkGenerator {
    private final Registry<Biome> biomesRegistry;
    private final Array<Biome> biomes = new Array<>();
    private DomainWarping layerDomain;

    public SimpleChunkGenerator(Registry<Biome> biomeRegistry) {
        biomesRegistry = biomeRegistry;
    }

    @Override
    public void create(ServerWorld world, long seed) {
        NoiseConfigs noiseConfigs = world.getServer().getNoiseConfigs();
        layerDomain = new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed)));
    }

    @Override
    public void generate(ServerWorld world, BuilderChunk chunk, Collection<ServerWorld.@NotNull RecordedChange> recordedChanges) {
        Carver carver = getCarver();

        RecordingChunk recordingChunk = new RecordingChunk(chunk);

        this.generateTerrain(chunk, carver, recordedChanges);
        this.generateRecordedChanges(chunk, recordingChunk);
        this.generateFeatures(chunk, recordingChunk);
        this.generateStructures(chunk, recordingChunk);
    }

    @Override
    public DomainWarping getLayerDomain() {
        return layerDomain;
    }

    protected abstract void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver, @NotNull Collection<ServerWorld.@NotNull RecordedChange> recordedChanges);

    protected void generateRecordedChanges(BuilderChunk chunk, RecordingChunk recordingChunk) {
        for (ServerWorld.RecordedChange change : recordingChunk.deferredChanges()) {
            if (WorldGenDebugContext.isActive()) {
                CommonConstants.LOGGER.info("Recorded change: " + change);
            }

            if (DebugFlags.LOG_OUT_OF_BOUNDS.isEnabled() && (change.x() < 0 || change.x() >= CHUNK_SIZE || change.z() < 0 || change.z() >= CHUNK_SIZE)) {
                QuantumServer.LOGGER.warn("Recorded change out of bounds: {}", change);
            }

            chunk.set(change.x(), change.y(), change.z(), change.block());
        }
    }

    protected void generateFeatures(BuilderChunk builderChunk, RecordingChunk recordingChunk) {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int height = builderChunk.getWorld().getHeight(builderChunk.getOffset().x + x, builderChunk.getOffset().y + z, HeightmapType.WORLD_SURFACE);
                builderChunk.getBiomeGenerator(x, z).generateTerrainFeatures(recordingChunk, x, z, height);
            }
        }
    }

    protected void generateStructures(BuilderChunk chunk, RecordingChunk recordingChunk) {
        for (var x = 0; x < CHUNK_SIZE; x++) {
            for (var z = 0; z < CHUNK_SIZE; z++) {
                int highest = chunk.getWorld().getHeight(chunk.getOffset().x + x, chunk.getOffset().z + z, HeightmapType.WORLD_SURFACE);
                chunk.getBiomeGenerator(x, z).generateStructureFeatures(recordingChunk, x, highest, z);
            }
        }
    }

    protected final void addBiome(RegistryKey<Biome> biome) {
        biomes.add(biomesRegistry.get(biome));
    }

    protected abstract @NotNull Carver getCarver();

    @Override
    public void dispose() {

    }
}
