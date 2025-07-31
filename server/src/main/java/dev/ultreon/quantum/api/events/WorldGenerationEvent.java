package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.api.events.chunk.ChunkEvent;
import dev.ultreon.quantum.api.events.world.ServerWorldEvent;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public interface WorldGenerationEvent extends ServerWorldEvent, ChunkEvent {
    @Override
    BuilderChunk getChunk();

    @Override
    @NotNull
    default ServerWorld getWorld() {
        return getChunk().getWorld();
    }

    class Generate implements WorldGenerationEvent, Cancelable {
        private final BuilderChunk chunk;
        private final List<ServerWorld.RecordedChange> changes;
        private boolean canceled;

        public Generate(BuilderChunk chunk, List<ServerWorld.RecordedChange> changes) {
            this.chunk = chunk;
            this.changes = Collections.unmodifiableList(changes);
        }

        @Override
        public BuilderChunk getChunk() {
            return chunk;
        }

        public List<ServerWorld.RecordedChange> getChanges() {
            return changes;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    class PostGenerate implements WorldGenerationEvent {
        private final BuilderChunk chunk;

        public PostGenerate(BuilderChunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public BuilderChunk getChunk() {
            return chunk;
        }
    }
}
