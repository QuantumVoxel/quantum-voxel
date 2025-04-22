package dev.ultreon.quantum.api.events.chunk;

import dev.ultreon.quantum.world.ServerChunk;
import dev.ultreon.quantum.ubo.types.DataType;
import dev.ultreon.quantum.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;

public class ChunkLoadEvent extends ServerChunkEvent {
    private final @NotNull MapType tag;

    public ChunkLoadEvent(@NotNull ServerChunk chunk,
                          @NotNull MapType tag) {
        super(chunk);
        this.tag = tag;
    }

    public void setTag(String tag, DataType<?> value) {
        this.tag.put(tag, value);
    }

    public DataType<?> getTag(String tag) {
        return this.tag.get(tag);
    }
}
