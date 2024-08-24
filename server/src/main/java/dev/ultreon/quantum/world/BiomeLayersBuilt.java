package dev.ultreon.quantum.world;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.world.gen.WorldGenFeature;
import dev.ultreon.quantum.world.gen.layer.TerrainLayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BiomeLayersBuilt extends Event {
    private final @NotNull Biome biome;
    private final @NotNull List<@NotNull TerrainLayer> layers;
    private final @NotNull List<@NotNull WorldGenFeature> features;

    public BiomeLayersBuilt(@NotNull Biome biome,
                            @NotNull List<@NotNull TerrainLayer> layers,
                            @NotNull List<@NotNull WorldGenFeature> features) {
        this.biome = biome;
        this.layers = layers;
        this.features = features;
    }

    public @NotNull Biome getBiome() {
        return biome;
    }

    public @NotNull List<@NotNull TerrainLayer> getLayers() {
        return layers;
    }

    public @NotNull List<@NotNull WorldGenFeature> getFeatures() {
        return features;
    }
}
