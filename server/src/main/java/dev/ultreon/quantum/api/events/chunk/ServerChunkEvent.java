package dev.ultreon.quantum.api.events.chunk;

import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.world.ServerChunk;
import dev.ultreon.quantum.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public abstract class ServerChunkEvent extends ChunkEvent {
    public ServerChunkEvent(@NotNull ServerChunk chunk) {
        super(chunk);
    }

    @Override
    public @NotNull ServerChunk getChunk() {
        return (ServerChunk) super.getChunk();
    }

    public @NotNull QuantumServer getServer() {
        return getChunk().getWorld().getServer();
    }

    public @NotNull ServerWorld getWorld() {
        return getChunk().getWorld();
    }
}
