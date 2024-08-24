package dev.ultreon.quantum.world.gen;

import com.badlogic.gdx.utils.Disposable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex4DVariant;
import de.articdive.jnoise.pipeline.JNoise;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.Modifications;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.debug.WorldGenDebugContext;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.MathHelper;
import dev.ultreon.quantum.util.Vec2i;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.gen.biome.BiomeData;
import dev.ultreon.quantum.world.gen.biome.BiomeGenerator;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

public class TerrainGenerator implements Disposable {
    private final DomainWarping biomeDomain;
    private final DomainWarping layerDomain;
    private final NoiseConfig noiseConfig;
    private @MonotonicNonNull NoiseSource noise;
    private final List<BiomeData> biomeGenData = new ArrayList<>();
    private @Nullable Carver carver;
    private @Nullable BiomeNoise humidNoise = null;
    private @Nullable BiomeNoise tempNoise = null;
    private @Nullable BiomeNoise variationNoise = null;
    private @Nullable HillinessNoise hillinessNoise = null;

    public TerrainGenerator(DomainWarping biomeDomain, DomainWarping layerDomain, NoiseConfig noiseConfig) {
        this.biomeDomain = biomeDomain;
        this.layerDomain = layerDomain;
        this.noiseConfig = noiseConfig;
    }

    public void create(ServerWorld world, long seed) {
        this.noise = JNoise.newBuilder()
                .fastSimplex(seed, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                .abs()
                .build();

        TerrainNoise noise = new TerrainNoise(world.getSeed());
        this.humidNoise = new BiomeNoise(world.getSeed() + 200);
        this.tempNoise = new BiomeNoise(world.getSeed() + 210);
        this.variationNoise = new BiomeNoise(world.getSeed() + 220);
        this.hillinessNoise = new HillinessNoise(world.getSeed() + 230);
        this.carver = new Carver(biomeDomain, noise, world.getSeed() + 300);
    }

    @CanIgnoreReturnValue
    public BiomeData registerBiome(ServerWorld world, long seed, Biome biome, float temperatureStart, float temperatureEnd, float humidityStart, float humidityEnd, boolean isOcean) {
        return registerBiome(world, seed, biome, temperatureStart, temperatureEnd, humidityStart, humidityEnd, -64.0f, 320.0f, -2.0f, 2.0f, isOcean);
    }

    @CanIgnoreReturnValue
    public BiomeData registerBiome(ServerWorld world, long seed, Biome biome, float temperatureStart, float temperatureEnd, float humidityStart, float humidityEnd, float heightStart, float heightEnd, float hillinessStart, float hillinessEnd, boolean isOcean) {
        var generator = biome.create(world, seed);
        var biomeData = new BiomeData(temperatureStart, temperatureEnd, humidityStart, humidityEnd, heightStart, heightEnd, hillinessStart, hillinessEnd, isOcean, generator);
        this.biomeGenData.add(biomeData);
        return biomeData;
    }

    @CanIgnoreReturnValue
    public BuilderChunk generate(@NotNull BuilderChunk chunk, @NotNull Collection<ServerWorld.@NotNull RecordedChange> recordedChanges) {
        Carver carver = this.carver;
        if (carver == null) throw new IllegalStateException("Carver has not been initialized yet!");

        RecordingChunk recordingChunk = new RecordingChunk(chunk);

        generateTerrain(chunk, carver, recordedChanges);

        generateRecordedChanges(chunk, recordingChunk);
        generateFeatures(chunk, recordingChunk);
        generateStructures(chunk, recordingChunk);

        return chunk;
    }

    private void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver, @NotNull Collection<ServerWorld.@NotNull RecordedChange> recordedChanges) {
        if (this.hillinessNoise == null)
            throw new IllegalStateException("Hilliness noise has not been initialized yet!");
        BlockVec offset = chunk.getOffset();
        for (var x = 0; x < CHUNK_SIZE; x++) {
            for (var z = 0; z < CHUNK_SIZE; z++) {
                double hilliness = this.hillinessNoise.evaluateNoise(offset.x + x, offset.z + z) - 2.0f;
                int groundPos = carver.carve(chunk, offset.x + x, offset.z + z, hilliness);

                var index = this.findGenerator(new Vec3i(offset.x + x, 0, offset.z + z), groundPos);
                chunk.setBiomeGenerator(x, z, index.biomeGenerator);
                index.biomeGenerator.processColumn(chunk, x, groundPos, z, recordedChanges);
            }
        }
    }

    private static void generateRecordedChanges(BuilderChunk chunk, RecordingChunk recordingChunk) {
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

    private static void generateFeatures(BuilderChunk builderChunk, RecordingChunk recordingChunk) {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int height = builderChunk.getHeight(x, z, HeightmapType.WORLD_SURFACE);
                builderChunk.getBiomeGenerator(x, z).generateTerrainFeatures(recordingChunk, x, z, height);
            }
        }
    }

    private static void generateStructures(BuilderChunk chunk, RecordingChunk recordingChunk) {
        for (var x = 0; x < CHUNK_SIZE; x++) {
            for (var z = 0; z < CHUNK_SIZE; z++) {
                int highest = chunk.getHeight(x, z, HeightmapType.WORLD_SURFACE);
                chunk.getBiomeGenerator(x, z).generateStructureFeatures(recordingChunk, x, highest, z);
            }
        }
    }

    private BiomeGenerator.Index findGenerator(Vec3i offset, int height) {
        return this.findGenerator(offset, height, Modifications.INSTANCE.getEnableDomainWarping());
    }

    private BiomeGenerator.Index findGenerator(Vec3i offset, int height, boolean useDomainWarping) {
        if (useDomainWarping) {
            Vec2i domainOffset = MathHelper.round(this.biomeDomain.generateDomainOffset(offset.x, offset.z));
            offset.add(domainOffset.x, 0, domainOffset.y);
        }

        if (this.humidNoise == null || this.tempNoise == null || this.variationNoise == null)
            throw new IllegalStateException("Biome generator noise has not been initialized yet!");

        var humid = this.humidNoise.evaluateNoise(offset.x * this.noiseConfig.noiseZoom(), offset.z * this.noiseConfig.noiseZoom()) * 2.0f;
        var temp = this.tempNoise.evaluateNoise(offset.x * this.noiseConfig.noiseZoom(), offset.z * this.noiseConfig.noiseZoom()) * 2.0f;
        var variation = this.variationNoise.evaluateNoise(offset.x * this.noiseConfig.noiseZoom(), offset.z * this.noiseConfig.noiseZoom()) * 2.0f;
        BiomeGenerator biomeGen = selectGenerator(height, humid, temp, variation);

        return new BiomeGenerator.Index(biomeGen);

    }

    private BiomeGenerator selectGenerator(int height, double humid, double temp, double variation) {
        BiomeGenerator biomeGen = null;

        if (variation < -2.0 || variation > 2.0) {
            CommonConstants.LOGGER.warn("Invalid variation: " + variation);
            return this.biomeGenData.getFirst().biomeGen();
        }

        if (temp < -2.0 || temp > 2.0) {
            CommonConstants.LOGGER.warn("Invalid temperature: " + temp);
            return this.biomeGenData.getFirst().biomeGen();
        }

        if (humid < -2.0 || humid > 2.0) {
            CommonConstants.LOGGER.warn("Invalid humidity: " + humid);
            return this.biomeGenData.getFirst().biomeGen();
        }

        if (height < -64.0 || height > 320.0) {
            CommonConstants.LOGGER.warn("Invalid height: " + height);
            return this.biomeGenData.getFirst().biomeGen();
        }

        for (var data : this.biomeGenData) {
            var currentlyOcean = height < World.SEA_LEVEL - 4;

            boolean validHeight = height >= data.heightStartThreshold() && height < data.heightEndThreshold();
            boolean validHumid = humid >= data.humidityStartThreshold() && humid < data.humidityEndThreshold();
            boolean validTemp = temp >= data.temperatureStartThreshold() && temp < data.temperatureEndThreshold();
            boolean validVar = variation >= data.variationStartThreshold() && variation < data.variationEndThreshold();
            if (validTemp && data.isOcean() == currentlyOcean && validHumid && validHeight && validVar)
                biomeGen = data.biomeGen();
        }

        if (biomeGen == null) {
            CommonConstants.LOGGER.warn("No biome generator found for height: " + height + ", humid: " + humid + ", temp: " + temp + ", variation: " + variation);
            return this.biomeGenData.getFirst().biomeGen();
        }

        return biomeGen;
    }

    public DomainWarping getLayerDomain() {
        return this.layerDomain;
    }

    @Override
    public void dispose() {
        this.biomeDomain.dispose();
        this.layerDomain.dispose();
        this.biomeGenData.forEach(data -> data.biomeGen().dispose());
    }

    public @MonotonicNonNull NoiseSource getNoise() {
        return noise;
    }
}
