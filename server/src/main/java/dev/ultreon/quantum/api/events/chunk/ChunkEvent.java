package dev.ultreon.quantum.api.events.chunk;

import dev.ultreon.quantum.api.events.world.WorldAccessEvent;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public interface ChunkEvent extends WorldAccessEvent {
    @Nullable Chunk getChunk();

    @Override
    default @Nullable WorldAccess getWorld() {
        if(getChunk() == null) {
            return null;
        }
        return getChunk().getWorld();
    }
}
