package dev.ultreon.quantum.api.events.chunk;

import dev.ultreon.quantum.ubo.types.DataType;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.ServerChunk;
import org.jetbrains.annotations.NotNull;

public class ChunkSaveEvent extends ServerChunkEvent {
    private final @NotNull MapType tag;

    public ChunkSaveEvent(@NotNull ServerChunk chunk,
                          @NotNull MapType tag) {
        super(chunk);
        this.tag = tag;
    }

    public void setTag(String key, DataType<?> data) {
        this.tag.put(key, data);
    }

    public DataType<?> getTag(String key) {
        return this.tag.get(key);
    }
}
