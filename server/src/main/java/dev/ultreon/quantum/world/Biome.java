package dev.ultreon.quantum.world;

import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.events.WorldEvents;
import dev.ultreon.quantum.events.WorldLifecycleEvents;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.gen.TerrainFeature;
import dev.ultreon.quantum.world.gen.biome.BiomeGenerator;
import dev.ultreon.quantum.world.gen.layer.GroundTerrainLayer;
import dev.ultreon.quantum.world.gen.layer.SurfaceTerrainLayer;
import dev.ultreon.quantum.world.gen.layer.TerrainLayer;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import dev.ultreon.quantum.world.gen.noise.NoiseConfigs;
import dev.ultreon.quantum.ubo.types.MapType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Biome class represents a specific type of terrain with defined characteristics like temperature,
 * humidity, height, and hilliness.
 * It includes terrain layers and world generation features.
 * Biome instances are built using the nested {@link Biome.Builder} class.
 */
public abstract class Biome {
    protected final List<TerrainLayer> layers = new ArrayList<>();
    protected final List<TerrainFeature> surfaceFeatures = new ArrayList<>();
    protected final List<TerrainFeature> undergroundFeatures = new ArrayList<>();
    private final float temperatureStart;
    private final float temperatureEnd;
    private final boolean isOcean;
    private final boolean doesNotGenerate;
    private final float humidityStart;
    private final float humidityEnd;
    private final float heightStart;
    private final float heightEnd;
    private final float hillinessStart;
    private final float hillinessEnd;

    protected Biome(float temperatureStart,
                    float temperatureEnd,
                    boolean isOcean,
                    boolean doesNotGenerate,
                    float humidityStart,
                    float humidityEnd,
                    float heightStart,
                    float heightEnd,
                    float hillinessStart,
                    float hillinessEnd) {
        this.temperatureStart = temperatureStart;
        this.temperatureEnd = temperatureEnd;
        this.isOcean = isOcean;
        this.doesNotGenerate = doesNotGenerate;
        this.humidityStart = humidityStart;
        this.humidityEnd = humidityEnd;
        this.heightStart = heightStart;
        this.heightEnd = heightEnd;
        this.hillinessStart = hillinessStart;
        this.hillinessEnd = hillinessEnd;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final void buildLayers() {
        this.onBuildLayers(this.layers, this.surfaceFeatures);

        ModApi.getGlobalEventHandler().call(new BiomeLayersBuilt(this, this.layers, this.surfaceFeatures));
        WorldLifecycleEvents.BIOME_LAYERS_BUILT.factory().onBiomeLayersBuilt(this, this.layers, this.surfaceFeatures);
    }

    protected abstract void onBuildLayers(List<TerrainLayer> layers, List<TerrainFeature> features);

    public boolean doesNotGenerate() {
        return this.doesNotGenerate;
    }

    public boolean isValidForHeight(float height) {
        return height >= this.heightStart && height < this.heightEnd;
    }

    public BiomeGenerator create(ServerWorld world, long seed) {
//        WorldEvents.CREATE_BIOME.factory().onCreateBiome(world, world.getGenerator().getLayerDomain(), this.layers, this.surfaceFeatures);

        this.layers.forEach(layer -> layer.create(world));
        this.surfaceFeatures.forEach(feature -> feature.create(world));
        this.undergroundFeatures.forEach(feature -> feature.create(world));

        NoiseConfigs noiseConfigs = world.getServer().getNoiseConfigs();
        DomainWarping domainWarping = new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed)));

        return new BiomeGenerator(world, this, this.layers, this.surfaceFeatures, this.undergroundFeatures);
    }

    public float getTemperatureStart() {
        return this.temperatureStart;
    }

    public float getTemperatureEnd() {
        return this.temperatureEnd;
    }

    public MapType save(QuantumServer server) {
        MapType mapType = new MapType();
        mapType.putString("id", String.valueOf(server.getRegistries().get(RegistryKeys.BIOME).getKey(this).id()));
        return mapType;
    }

    public static Biome load(QuantumServer server, MapType mapType) {
        Biome biome = QuantumServer.get().getRegistries().get(RegistryKeys.BIOME).get(NamespaceID.tryParse(mapType.getString("id", "plains")));
        return biome != null ? biome : server.getBiomes().plains;
    }

    public boolean isOcean() {
        return this.isOcean;
    }

    public boolean isTopBlock(BlockState currentBlock) {
        if (currentBlock.getBlock() == Blocks.AIR) return true;
        return layers.stream().anyMatch(terrainLayer -> terrainLayer instanceof SurfaceTerrainLayer && ((SurfaceTerrainLayer) terrainLayer).surfaceBlock == currentBlock.getBlock());
    }

    public BlockState getTopMaterial() {
        return layers.stream().map(terrainLayer -> terrainLayer instanceof SurfaceTerrainLayer ? ((SurfaceTerrainLayer) terrainLayer).surfaceBlock : null).filter(Objects::nonNull).findFirst().map(Block::getDefaultState).orElse(null);
    }

    public BlockState getFillerMaterial() {
        return layers.stream().map(terrainLayer -> terrainLayer instanceof GroundTerrainLayer ? ((GroundTerrainLayer) terrainLayer).block : null).filter(Objects::nonNull).findFirst().map(Block::getDefaultState).orElse(null);
    }

    public float getHumidityStart() {
        return humidityStart;
    }

    public float getHumidityEnd() {
        return humidityEnd;
    }

    public float getHeightStart() {
        return this.heightStart;
    }

    public float getHeightEnd() {
        return this.heightEnd;
    }

    public float getHillinessStart() {
        return this.hillinessStart;
    }

    public float getHillinessEnd() {
        return this.hillinessEnd;
    }

    /**
     * Builder class for constructing {@link Biome} instances with various configurations.
     * This builder allows setting noise configurations, terrain layers, world generation features,
     * temperature, humidity, height, and hilliness ranges, as well as marking the biome as an ocean
     * or a non-generating biome.
     */
    public static class Builder {
        @Nullable
        private NoiseConfig biomeNoise;
        private final List<TerrainLayer> layers = new ArrayList<>();
        private final List<TerrainFeature> surfaceFeatures = new ArrayList<>();
        private final List<TerrainFeature> undergroundFeatures = new ArrayList<>();
        private float temperatureStart = -2.0f;
        private float temperatureEnd = 2.0f;
        private float humidityStart = -2.0f;
        private float humidityEnd = 2.0f;
        private float heightStart = -64f;
        private float heightEnd = 320f;
        private float hillinessStart = -2.0f;
        private float hillinessEnd = 2.0f;
        private boolean isOcean;
        private boolean doesNotGenerate;

        private Builder() {

        }

        public Builder noise(NoiseConfig biomeNoise) {
            this.biomeNoise = biomeNoise;
            return this;
        }

        public Builder layer(TerrainLayer layer) {
            this.layers.add(layer);
            return this;
        }

        public Builder surfaceFeature(TerrainFeature feature) {
            this.surfaceFeatures.add(feature);
            return this;
        }

        public Builder undergroundFeature(TerrainFeature feature) {
            this.undergroundFeatures.add(feature);
            return this;
        }

        @Deprecated
        public Builder temperatureStart(float temperatureStart) {
            this.temperatureStart = temperatureStart;
            return this;
        }

        @Deprecated
        public Builder temperatureEnd(float temperatureEnd) {
            this.temperatureEnd = temperatureEnd;
            return this;
        }

        @Deprecated
        public Builder humidityStart(float humidityStart) {
            this.humidityStart = humidityStart;
            return this;
        }

        @Deprecated
        public Builder humidityEnd(float humidityEnd) {
            this.humidityEnd = humidityEnd;
            return this;
        }

        public Builder temperatureRange(float temperatureStart, float temperatureEnd) {
            this.temperatureStart = temperatureStart;
            this.temperatureEnd = temperatureEnd;
            return this;
        }

        public Builder humidityRange(float humidityStart, float humidityEnd) {
            this.humidityStart = humidityStart;
            this.humidityEnd = humidityEnd;
            return this;
        }

        public Builder heightRange(float heightStart, float heightEnd) {
            this.heightStart = heightStart;
            this.heightEnd = heightEnd;
            return this;
        }

        public Builder hillinessRange(float hillinessStart, float hillinessEnd) {
            this.hillinessStart = hillinessStart;
            this.hillinessEnd = hillinessEnd;
            return this;
        }

        public Builder ocean() {
            this.isOcean = true;
            return this;
        }

        public Biome build() {
            if (Float.isNaN(this.temperatureStart)) throw new IllegalArgumentException("Temperature start not set.");
            if (Float.isNaN(this.temperatureEnd)) throw new IllegalArgumentException("Temperature end not set.");

            return new Biome(this.temperatureStart, this.temperatureEnd, this.isOcean, this.doesNotGenerate, this.humidityStart, this.humidityEnd, this.heightStart, this.heightEnd, this.hillinessStart, this.hillinessEnd) {
                @Override
                protected void onBuildLayers(List<TerrainLayer> layerList, List<TerrainFeature> featureList) {
                    this.layers.addAll(Builder.this.layers);
                    this.surfaceFeatures.addAll(Builder.this.surfaceFeatures);
                    this.undergroundFeatures.addAll(Builder.this.undergroundFeatures);
                }
            };
        }

        public Builder doesNotGenerate() {
            this.doesNotGenerate = true;
            return this;
        }
    }
}
