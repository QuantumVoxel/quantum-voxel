package dev.ultreon.quantum.api.events.world;

import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public interface ServerWorldEvent extends WorldEvent {
    @Override
    @NotNull ServerWorld getWorld();

    class WorldLoadEvent implements ServerWorldEvent {
        @NotNull
        private final ServerWorld world;

        public WorldLoadEvent(@NotNull ServerWorld world) {
            this.world = world;
        }

        @Override
        public @NotNull ServerWorld getWorld() {
            return world;
        }
    }

    class WorldUnloadEvent implements ServerWorldEvent {
        @NotNull
        private final ServerWorld world;

        public WorldUnloadEvent(@NotNull ServerWorld world) {
            this.world = world;
        }

        @Override
        public @NotNull ServerWorld getWorld() {
            return world;
        }
    }

    class WorldSaveEvent implements ServerWorldEvent {
        @NotNull
        private final ServerWorld world;
        private final MapType worldData;

        public WorldSaveEvent(@NotNull ServerWorld world, MapType worldData) {
            this.world = world;
            this.worldData = worldData;
        }

        public MapType getWorldData() {
            return worldData;
        }

        @Override
        public @NotNull ServerWorld getWorld() {
            return world;
        }
    }
}
