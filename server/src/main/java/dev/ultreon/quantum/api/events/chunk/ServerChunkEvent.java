package dev.ultreon.quantum.api.events.chunk;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.api.events.ServerEvent;
import dev.ultreon.quantum.api.events.world.ServerWorldEvent;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.ServerChunk;
import dev.ultreon.quantum.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public interface ServerChunkEvent extends ChunkEvent, ServerWorldEvent, ServerEvent {
    @Override
    @NotNull ServerChunk getChunk();
    
    default @NotNull ServerWorld getWorld() {
        return getChunk().getWorld();
    }
    
    @Override
    default @NotNull QuantumServer getServer() {
        return getWorld().getServer();
    }

    class Load implements ServerChunkEvent {
        @NotNull
        private final ServerChunk chunk;

        public Load(@NotNull ServerChunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public @NotNull ServerChunk getChunk() {
            return chunk;
        }
    }

    class LoadData implements ServerChunkEvent {
        @NotNull
        private final ServerChunk chunk;
        private final MapType extra;

        public LoadData(@NotNull ServerChunk chunk, MapType extra) {
            this.chunk = chunk;
            this.extra = extra;
        }

        @Override
        public @NotNull ServerChunk getChunk() {
            return chunk;
        }

        public MapType getExtra() {
            return extra;
        }
    }

    class SaveData implements ServerChunkEvent {
        @NotNull
        private final ServerChunk chunk;
        private final MapType extra;

        public SaveData(@NotNull ServerChunk chunk, MapType extra) {
            this.chunk = chunk;
            this.extra = extra;
        }

        @Override
        public @NotNull ServerChunk getChunk() {
            return chunk;
        }

        public MapType getExtra() {
            return extra;
        }
    }

    class UnloadEvent implements Cancelable, ServerChunkEvent {
        @NotNull
        private final ServerChunk chunk;
        private boolean canceled;

        public UnloadEvent(@NotNull ServerChunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        @Override
        public @NotNull ServerChunk getChunk() {
            return chunk;
        }
    }
}
