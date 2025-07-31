package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.api.events.chunk.ChunkEvent;
import dev.ultreon.quantum.client.world.ChunkModel;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.world.ChunkBuildInfo;
import org.jetbrains.annotations.Nullable;

public interface ClientChunkEvent extends ChunkEvent, ClientWorldEvent {
    @Override
    @Nullable ClientChunk getChunk();

    @Override
    default ClientWorld getWorld() {
        return getChunk().getWorld();
    }

    class ChunkLoaded implements ClientChunkEvent {
        private final ClientChunk chunk;

        public ChunkLoaded(ClientChunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public ClientChunk getChunk() {
            return chunk;
        }
    }

    class Received implements ClientChunkEvent {
        @Nullable
        private final ClientChunk chunk;
        private final ChunkBuildInfo info;

        public Received(@Nullable ClientChunk chunk, ChunkBuildInfo info) {
            this.chunk = chunk;
            this.info = info;
        }

        public ChunkBuildInfo getInfo() {
            return info;
        }

        @Override
        public @Nullable ClientChunk getChunk() {
            return chunk;
        }
    }

    class Rebuilt implements ClientChunkEvent {
        private final ClientChunk chunk;

        public Rebuilt(ClientChunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public ClientChunk getChunk() {
            return chunk;
        }
    }

    class Built implements ClientChunkEvent {
        private final ClientChunk chunk;

        public Built(ClientChunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public ClientChunk getChunk() {
            return chunk;
        }
    }

    class ModelBuilt implements ClientChunkEvent {
        private final ClientChunk chunk;
        private final ChunkModel model;

        public ModelBuilt(ClientChunk chunk, ChunkModel model) {
            this.chunk = chunk;
            this.model = model;
        }

        @Override
        public ClientChunk getChunk() {
            return chunk;
        }

        public ChunkModel getModel() {
            return model;
        }
    }
}
