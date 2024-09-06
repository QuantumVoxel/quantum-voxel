package dev.ultreon.quantum.events;

import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.gen.TerrainFeature;
import dev.ultreon.quantum.world.gen.layer.TerrainLayer;

import java.util.List;

public class WorldLifecycleEvents {
    public static final Event<WorldLifecycleEvents.BiomeLayersBuilt> BIOME_LAYERS_BUILT = Event.create();

    @FunctionalInterface
    public interface BiomeLayersBuilt {
        void onBiomeLayersBuilt(Biome biome, List<TerrainLayer> layers, List<TerrainFeature> features);
    }
}
