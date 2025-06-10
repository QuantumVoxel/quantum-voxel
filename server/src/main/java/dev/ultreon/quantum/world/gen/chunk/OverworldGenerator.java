package dev.ultreon.quantum.world.gen.chunk;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.Modifications;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.util.MathHelper;
import dev.ultreon.quantum.util.Vec2i;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.*;
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
import java.util.List;

import static dev.ultreon.quantum.world.World.CS;

/**
 * The OverworldGenerator is responsible for generating the terrain of the Overworld.
 * It uses various noise configurations and biome data to create diverse and immersive biomes.
 * It extends the SimpleChunkGenerator, inheriting its basic functionalities and providing further customization.
 */
public class OverworldGenerator extends SimpleChunkGenerator {
    private final List<BiomeData> biomeGenData = new ArrayList<>();

    private NoiseConfig noiseConfig;

    private @UnknownNullability DomainWarping biomeDomain;
    private @UnknownNullability BiomeNoise humidNoise;
    private @UnknownNullability BiomeNoise tempNoise;
    private @UnknownNullability BiomeNoise variationNoise;
    private @UnknownNullability Carver carver;

    public OverworldGenerator(Registry<Biome> biomeRegistry) {
        super(biomeRegistry);

        biomeRegistry.keys().forEach(this::addBiome);
    }

    @Override
    public void create(@NotNull ServerWorld world, long seed) {
        NoiseConfigs noiseConfigs = world.getServer().getNoiseConfigs();
        noiseConfig = noiseConfigs.biomeMap;
        this.biomeDomain = new DomainWarping(noiseConfigs.biomeX.create(seed + 100), noiseConfigs.biomeY.create(seed + 110));

        TerrainNoise noise = new TerrainNoise(world.getSeed());
        this.humidNoise = new BiomeNoise(world.getSeed() + 200);
        this.tempNoise = new BiomeNoise(world.getSeed() + 210);
        this.variationNoise = new BiomeNoise(world.getSeed() + 220);
        this.carver = new OverworldCarver(biomeDomain, noise, world.getSeed() + 300);

        for (Biome biome : this.biomes.toArray(Biome.class)) {
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
    protected void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver) {
        BlockVec offset = chunk.getOffset();
        for (var x = 0; x < CS; x++) {
            for (var z = 0; z < CS; z++) {
                float groundPos = carver.carve(chunk, offset.x + x, offset.z + z);

                var index = this.findGenerator(new Vec3i(offset.x + x, 0, offset.z + z), groundPos);
                chunk.setBiomeGenerator(x, z, index.biomeGenerator);
                index.biomeGenerator.processColumn(chunk, x, (int) Math.floor(groundPos), z);
            }
        }
    }

    /**
     * Finds the appropriate biome generator index based on the provided offset and height.
     *
     * @param offset the vector offset to locate the generator
     * @param height the height to determine which generator to use
     * @return the biome generator index for the specified offset and height
     */
    public BiomeGenerator.Index findGenerator(Vec3i offset, float height) {
        return this.findGenerator(offset, height, Modifications.enableDomainWarping);
    }

    /**
     * Finds the appropriate biome generator index based on the provided offset, height, and domain warping preference.
     *
     * @param offset the vector offset to locate the generator
     * @param height the height to determine which generator to use
     * @param useDomainWarping flag indicating whether to apply domain warping to the offset
     * @return the biome generator index for the specified offset and height
     */
    public BiomeGenerator.Index findGenerator(Vec3i offset, float height, boolean useDomainWarping) {
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

    /**
     * Selects an appropriate biome generator based on provided environmental parameters such as height, humidity, temperature, and variation.
     *
     * @param height the vertical position, used to determine the terrain's elevation
     * @param humid the humidity level, which influences biome moisture characteristics
     * @param temp the temperature level, affecting biome heat properties
     * @param variation the variation index, used to account for terrain irregularities
     * @return the selected {@link BiomeGenerator} that matches the given parameters; if no suitable generator is found, a default generator is returned
     */
    public BiomeGenerator selectGenerator(float height, double humid, double temp, double variation) {
        BiomeGenerator biomeGen = null;

        if (variation < -2.0 || variation > 2.0) {
            CommonConstants.LOGGER.warn("Invalid variation: {}", variation);
            return this.biomeGenData.get(0).biomeGen();
        }

        if (temp < -2.0 || temp > 2.0) {
            CommonConstants.LOGGER.warn("Invalid temperature: {}", temp);
            return this.biomeGenData.get(0).biomeGen();
        }

        if (humid < -2.0 || humid > 2.0) {
            CommonConstants.LOGGER.warn("Invalid humidity: {}", humid);
            return this.biomeGenData.get(0).biomeGen();
        }

        if (height < -64.0 || height > 320.0) {
            CommonConstants.LOGGER.warn("Invalid height: {}", height);
            return this.biomeGenData.get(0).biomeGen();
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
            CommonConstants.LOGGER.warn("No biome generator found for height: {}, humid: {}, temp: {}, variation: {}", height, humid, temp, variation);
            return this.biomeGenData.get(0).biomeGen();
        }

        return biomeGen;
    }

    @Override
    @NotNull
    public Carver getCarver() {
        if (carver == null) throw new IllegalStateException("Carver not initialized yet!");
        return carver;
    }

    @Override
    public double getTemperature(int x, int z) {
        return this.tempNoise.evaluateNoise(x * this.noiseConfig.noiseZoom(), z * this.noiseConfig.noiseZoom()) * 2.0f;
    }
}
