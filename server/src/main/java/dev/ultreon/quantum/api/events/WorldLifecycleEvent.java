package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.api.events.world.WorldEvent;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.WorldStorage;
import dev.ultreon.quantum.world.gen.TerrainFeature;
import dev.ultreon.quantum.world.gen.layer.TerrainLayer;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WorldLifecycleEvent extends WorldEvent, LifecycleEvent {
    class CreateBiome implements WorldLifecycleEvent {
        private final ServerWorld world;
        private final Biome biome;
        private final DomainWarping layerDomain;
        private final List<TerrainLayer> layers;
        private final List<TerrainFeature> surfaceFeatures;

        public CreateBiome(ServerWorld world, Biome biome, DomainWarping layerDomain, List<TerrainLayer> layers, List<TerrainFeature> surfaceFeatures) {
            this.world = world;
            this.biome = biome;
            this.layerDomain = layerDomain;
            this.layers = layers;
            this.surfaceFeatures = surfaceFeatures;
        }

        public List<TerrainFeature> getSurfaceFeatures() {
            return surfaceFeatures;
        }

        public List<TerrainLayer> getLayers() {
            return layers;
        }

        public DomainWarping getLayerDomain() {
            return layerDomain;
        }

        @Override
        public @NotNull ServerWorld getWorld() {
            return world;
        }

        public Biome getBiome() {
            return biome;
        }

        @Override
        public State getState() {
            return State.START;
        }
    }

    class Save implements WorldLifecycleEvent, Cancelable {
        private final ServerWorld world;
        private final boolean silent;
        private boolean canceled;

        public Save(ServerWorld world, boolean silent) {
            this.world = world;
            this.silent = silent;
        }

        @Override
        public @NotNull ServerWorld getWorld() {
            return world;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        public boolean isSilent() {
            return silent;
        }

        @Override
        public State getState() {
            return State.SAVE;
        }
    }

    class Saved implements WorldLifecycleEvent {
        private final ServerWorld world;
        private final boolean silent;
        private final WorldStorage storage;

        public Saved(ServerWorld world, boolean silent, WorldStorage storage) {
            this.world = world;
            this.silent = silent;
            this.storage = storage;
        }

        @Override
        public @NotNull ServerWorld getWorld() {
            return world;
        }

        public WorldStorage getStorage() {
            return storage;
        }

        public boolean isSilent() {
            return silent;
        }

        @Override
        public State getState() {
            return State.SAVE;
        }
    }

    class Loaded implements WorldLifecycleEvent {
        private final ServerWorld world;
        private final WorldStorage storage;

        public Loaded(ServerWorld world, WorldStorage storage) {
            this.world = world;
            this.storage = storage;
        }

        public WorldStorage getStorage() {
            return storage;
        }

        @Override
        public @NotNull ServerWorld getWorld() {
            return world;
        }

        @Override
        public State getState() {
            return State.START;
        }
    }

    class Unload implements WorldLifecycleEvent {
        private final ServerWorld world;

        public Unload(ServerWorld world) {
            this.world = world;
        }

        @Override
        public @NotNull ServerWorld getWorld() {
            return world;
        }

        @Override
        public State getState() {
            return State.STOP;
        }
    }
}
