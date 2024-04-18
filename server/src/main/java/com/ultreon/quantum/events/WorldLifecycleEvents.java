package com.ultreon.quantum.events;

import com.ultreon.quantum.events.api.Event;
import com.ultreon.quantum.world.Biome;
import com.ultreon.quantum.world.gen.WorldGenFeature;
import com.ultreon.quantum.world.gen.layer.TerrainLayer;

import java.util.List;

public class WorldLifecycleEvents {
    public static final Event<WorldLifecycleEvents.BiomeLayersBuilt> BIOME_LAYERS_BUILT = Event.create();

    @FunctionalInterface
    public interface BiomeLayersBuilt {
        void onBiomeLayersBuilt(Biome biome, List<TerrainLayer> layers, List<WorldGenFeature> features);
    }
}
