package dev.ultreon.quantum.world.gen;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.libs.commons.v0.vector.Vec2i;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
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
import dev.ultreon.quantum.util.BlockMetaPredicate;
import dev.ultreon.quantum.util.MathHelper;
import dev.ultreon.quantum.util.SanityCheckException;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.gen.biome.BiomeData;
import dev.ultreon.quantum.world.gen.biome.BiomeGenerator;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

public class TerrainGenerator implements Disposable {
    private final DomainWarping biomeDomain;
    private final DomainWarping layerDomain;
    private final NoiseConfig noiseConfig;
    @MonotonicNonNull
    private NoiseSource noise;
    private final List<BiomeData> biomeGenData = new ArrayList<>();
    @Nullable
    private Carver carver;

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
        this.carver = new Carver(biomeDomain, noise, world.getSeed() + 1);
    }

    @CanIgnoreReturnValue
    public BiomeData registerBiome(ServerWorld world, long seed, Biome biome, float temperatureStart, float temperatureEnd, boolean isOcean) {
        var generator = biome.create(world, seed);
        var biomeData = new BiomeData(temperatureStart, temperatureEnd, isOcean, generator);
        this.biomeGenData.add(biomeData);
        return biomeData;
    }

    @CanIgnoreReturnValue
    public BuilderChunk generate(BuilderChunk chunk, Collection<ServerWorld.RecordedChange> recordedChanges) {
//        this.buildBiomeCenters(chunk);

        if (this.carver == null) throw new SanityCheckException("Carver is not set!");

        RecordingChunk recordingChunk = new RecordingChunk(chunk);

        for (var x = 0; x < CHUNK_SIZE; x++) {
            for (var z = 0; z < CHUNK_SIZE; z++) {
                int groundPos = this.carver.carve(chunk, x, z);

                var index = this.findGenerator(new Vec3i(chunk.getOffset().x + x, 0, chunk.getOffset().z + z), groundPos);
                chunk.setBiomeGenerator(x, z, index.biomeGenerator);
                chunk = index.biomeGenerator.processColumn(chunk, x, groundPos, z, recordedChanges);
                index.biomeGenerator.generateTerrainFeatures(recordingChunk, x, z, chunk.getHighest(x, z, BlockMetaPredicate.WG_HEIGHT_CHK));
            }
        }

        for (ServerWorld.RecordedChange change : recordingChunk.getRecordedChanges()) {
            if (WorldGenDebugContext.isActive()) {
                CommonConstants.LOGGER.info("Recorded change: " + change);
            }

            if (DebugFlags.LOG_OUT_OF_BOUNDS.enabled() && (change.x() < 0 || change.x() >= CHUNK_SIZE || change.z() < 0 || change.z() >= CHUNK_SIZE)) {
                QuantumServer.LOGGER.warn("Recorded change out of bounds: {}", change);
            }

            chunk.set(change.x(), change.y(), change.z(), change.block());
        }

        return chunk;
    }

    public void buildBiomeCenters(BuilderChunk chunk) {
        var biomeCenters = this.evalBiomeCenters(chunk.getOffset());

        for (var biomeCenter : biomeCenters) {
            var domainWarpingOffset = this.biomeDomain.generateDomainOffsetInt(biomeCenter.x, biomeCenter.z);
            biomeCenter.add(new Vec3i(domainWarpingOffset.x, 0, domainWarpingOffset.y));
        }
        chunk.setBiomeCenters(biomeCenters);
    }

    private List<Vec3i> evalBiomeCenters(Vec3i pos) {
        int len = CHUNK_SIZE;

        Vec3i origin = new Vec3i(Math.round((float) pos.x) / len, 0, Math.round((float) pos.z));
        var centers = new ListOrderedSet<Vec3i>();

        centers.add(origin);

        for (var dir : Neighbour8Direction.values()) {
            var offXZ = dir.vec();

            centers.add(new Vec3i(origin.x + offXZ.x * len, 0, origin.z + offXZ.y * len));
            centers.add(new Vec3i(origin.x + offXZ.x * len, 0, origin.z + offXZ.y * 2 * len));
            centers.add(new Vec3i(origin.x + offXZ.x * 2 * len, 0, origin.z + offXZ.y * len));
            centers.add(new Vec3i(origin.x + offXZ.x * 2 * len, 0, origin.z + offXZ.y * 2 * len));
        }

        return centers.asList();
    }

    private BiomeGenerator.Index findGenerator(Vec3i offset, int height) {
        return this.findGenerator(offset, height, Modifications.INSTANCE.getEnableDomainWarping());
    }

    private BiomeGenerator.Index findGenerator(Vec3i offset, int height, boolean useDomainWarping) {
        if (useDomainWarping) {
            Vec2i domainOffset = MathHelper.round(this.biomeDomain.generateDomainOffset(offset.x, offset.z));
            offset.add(domainOffset.x, 0, domainOffset.y);
        }

        var temp = this.noise.evaluateNoise(offset.x * this.noiseConfig.noiseZoom(), offset.z * this.noiseConfig.noiseZoom()) * 2.0f;
        BiomeGenerator biomeGen = this.biomeGenData.get(0).biomeGen();

        for (var data : this.biomeGenData) {
            var currentlyOcean = height < World.SEA_LEVEL - 4;
            if (temp >= data.temperatureStartThreshold() && temp < data.temperatureEndThreshold() && data.isOcean() == currentlyOcean)
                biomeGen = data.biomeGen();
        }

        return new BiomeGenerator.Index(biomeGen);

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
}
