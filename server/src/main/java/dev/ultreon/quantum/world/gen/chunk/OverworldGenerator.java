package dev.ultreon.quantum.world.gen.chunk;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.Modifications;
import dev.ultreon.quantum.registry.ServerRegistry;
import dev.ultreon.quantum.tags.NamedTag;
import dev.ultreon.quantum.util.MathHelper;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Vec2i;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.gen.HillinessNoise;
import dev.ultreon.quantum.world.gen.biome.BiomeData;
import dev.ultreon.quantum.world.gen.biome.BiomeGenerator;
import dev.ultreon.quantum.world.gen.carver.Carver;
import dev.ultreon.quantum.world.gen.carver.OverworldCarver;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import dev.ultreon.quantum.world.gen.noise.NoiseConfigs;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

public class OverworldGenerator extends SimpleChunkGenerator {
    private final List<BiomeData> biomeGenData = new ArrayList<>();

    private NoiseConfig noiseConfig;
    private final NamedTag<Biome> biomeTag;

    private @UnknownNullability DomainWarping biomeDomain;
    private @UnknownNullability BiomeNoise humidNoise;
    private @UnknownNullability BiomeNoise tempNoise;
    private @UnknownNullability BiomeNoise variationNoise;
    private @UnknownNullability HillinessNoise hillinessNoise ;
    private @UnknownNullability Carver carver;

    public OverworldGenerator(ServerRegistry<Biome> biomeRegistry) {
        super(biomeRegistry);

        this.biomeTag = biomeRegistry.getTag(new NamespaceID("overworld_biomes")).orElseThrow();
    }

    @Override
    public void create(ServerWorld world, long seed) {
        NoiseConfigs noiseConfigs = world.getServer().getNoiseConfigs();
        noiseConfig = noiseConfigs.biomeMap;
        this.biomeDomain = new DomainWarping(noiseConfigs.biomeX.create(seed + 100), noiseConfigs.biomeY.create(seed + 110));

        TerrainNoise noise = new TerrainNoise(world.getSeed());
        this.humidNoise = new BiomeNoise(world.getSeed() + 200);
        this.tempNoise = new BiomeNoise(world.getSeed() + 210);
        this.variationNoise = new BiomeNoise(world.getSeed() + 220);
        this.hillinessNoise = new HillinessNoise(world.getSeed() + 230);
        this.carver = new OverworldCarver(biomeDomain, noise, world.getSeed() + 300);

        for (Biome biome : this.biomeTag.getValues()) {
            this.biomeGenData.add(new BiomeData(
                    biome.getTemperatureStart(), biome.getTemperatureEnd(),
                    biome.getHumidityStart(), biome.getHumidityEnd(),
                    biome.getHeightStart(), biome.getHeightEnd(),
                    biome.getHillinessStart(), biome.getHillinessEnd(),
                    biome.isOcean(), biome.create(world, seed)
            ));
        }
    }

    @Override
    protected void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver, @NotNull Collection<ServerWorld.@NotNull RecordedChange> recordedChanges) {
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

    @Override
    protected @NotNull Carver getCarver() {
        if (carver == null) throw new IllegalStateException("Carver not initialized yet!");
        return carver;
    }
}
