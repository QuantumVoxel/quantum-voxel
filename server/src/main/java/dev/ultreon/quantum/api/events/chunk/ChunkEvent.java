package dev.ultreon.quantum.api.events.chunk;

import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.WorldAccess;
import org.jetbrains.annotations.NotNull;

public abstract class ChunkEvent {
    private final @NotNull Chunk chunk;

    public ChunkEvent(@NotNull Chunk chunk) {
        this.chunk = chunk;
    }

    public @NotNull Chunk getChunk() {
        return chunk;
    }

    public WorldAccess getWorld() {
        return chunk.getWorld();
    }
}
