package dev.ultreon.quantum.world;

import com.google.common.base.Preconditions;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.world.gen.layer.GroundTerrainLayer;
import dev.ultreon.quantum.world.gen.layer.SurfaceTerrainLayer;
import dev.ultreon.ubo.types.MapType;
import dev.ultreon.quantum.events.WorldEvents;
import dev.ultreon.quantum.events.WorldLifecycleEvents;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.world.gen.WorldGenFeature;
import dev.ultreon.quantum.world.gen.biome.BiomeGenerator;
import dev.ultreon.quantum.world.gen.layer.TerrainLayer;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import dev.ultreon.quantum.world.gen.noise.NoiseConfigs;
import it.unimi.dsi.fastutil.longs.Long2ReferenceFunction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Biome {
    private final List<TerrainLayer> layers = new ArrayList<>();
    private final List<WorldGenFeature> features = new ArrayList<>();
    private final float temperatureStart;
    private final float temperatureEnd;
    private final boolean isOcean;
    private final boolean doesNotGenerate;

    protected Biome(float temperatureStart, float temperatureEnd, boolean isOcean, boolean doesNotGenerate) {
        this.temperatureStart = temperatureStart;
        this.temperatureEnd = temperatureEnd;
        this.isOcean = isOcean;
        this.doesNotGenerate = doesNotGenerate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final void buildLayers() {
        this.onBuildLayers(this.layers, this.features);

        WorldLifecycleEvents.BIOME_LAYERS_BUILT.factory().onBiomeLayersBuilt(this, this.layers, this.features);
    }

    protected abstract void onBuildLayers(List<TerrainLayer> layers, List<WorldGenFeature> features);

    public boolean doesNotGenerate() {
        return this.doesNotGenerate;
    }

    public BiomeGenerator create(ServerWorld world, long seed) {
        WorldEvents.CREATE_BIOME.factory().onCreateBiome(world, world.getTerrainGenerator().getLayerDomain(), this.layers, this.features);

        this.layers.forEach(layer -> layer.create(world));
        this.features.forEach(layer -> layer.create(world));

        DomainWarping domainWarping = new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed)));

        return new BiomeGenerator(world, this, domainWarping, this.layers, this.features);
    }

    public NoiseConfig getSettings() {
        return NoiseConfigs.BIOME_MAP;
    }

    public float getTemperatureStart() {
        return this.temperatureStart;
    }

    public float getTemperatureEnd() {
        return this.temperatureEnd;
    }

    public MapType save() {
        MapType mapType = new MapType();
        mapType.putString("id", String.valueOf(this.getId()));
        return mapType;
    }

    private Identifier getId() {
        return Registries.BIOME.getId(this);
    }

    public static Biome load(MapType mapType) {
        return Registries.BIOME.get(Identifier.tryParse(mapType.getString("id", "plains")));
    }

    public boolean isOcean() {
        return this.isOcean;
    }

    public boolean isTopBlock(BlockProperties currentBlock) {
        if (currentBlock.getBlock() == Blocks.AIR) return true;
        return layers.stream().anyMatch(terrainLayer -> terrainLayer instanceof SurfaceTerrainLayer layer && layer.surfaceBlock == currentBlock.getBlock());
    }

    public BlockProperties getTopMaterial() {
        return layers.stream().map(terrainLayer -> terrainLayer instanceof SurfaceTerrainLayer layer ? layer.surfaceBlock : null).filter(Objects::nonNull).findFirst().map(Block::createMeta).orElse(null);
    }

    public BlockProperties getFillerMaterial() {
        return layers.stream().map(terrainLayer -> terrainLayer instanceof GroundTerrainLayer layer ? layer.block : null).filter(Objects::nonNull).findFirst().map(Block::createMeta).orElse(null);
    }

    public static class Builder {
        @Nullable
        private NoiseConfig biomeNoise;
        private final List<TerrainLayer> layers = new ArrayList<>();
        private final List<WorldGenFeature> features = new ArrayList<>();
        private float temperatureStart = Float.NaN;
        private float temperatureEnd = Float.NaN;
        private boolean isOcean;
        private boolean doesNotGenerate;

        private Builder() {

        }

        public Builder noise(NoiseConfig biomeNoise) {
            this.biomeNoise = biomeNoise;
            return this;
        }

        public Builder domainWarping(Long2ReferenceFunction<DomainWarping> domainWarping) {
            return this;
        }

        public Builder layer(TerrainLayer layer) {
            this.layers.add(layer);
            return this;
        }

        public Builder feature(WorldGenFeature feature) {
            this.features.add(feature);
            return this;
        }

        public Builder temperatureStart(float temperatureStart) {
            this.temperatureStart = temperatureStart;
            return this;
        }

        public Builder temperatureEnd(float temperatureEnd) {
            this.temperatureEnd = temperatureEnd;
            return this;
        }

        public Builder ocean() {
            this.isOcean = true;
            return this;
        }

        public Biome build() {
            Preconditions.checkNotNull(this.biomeNoise, "Biome noise not set.");

            if (Float.isNaN(this.temperatureStart)) throw new IllegalArgumentException("Temperature start not set.");
            if (Float.isNaN(this.temperatureEnd)) throw new IllegalArgumentException("Temperature end not set.");

            return new Biome(this.temperatureStart, this.temperatureEnd, this.isOcean, this.doesNotGenerate) {
                @Override
                protected void onBuildLayers(List<TerrainLayer> layerList, List<WorldGenFeature> featureList) {
                    layerList.addAll(Builder.this.layers);
                    featureList.addAll(Builder.this.features);
                }
            };
        }

        public Builder doesNotGenerate() {
            this.doesNotGenerate = true;
            return this;
        }
    }
}
