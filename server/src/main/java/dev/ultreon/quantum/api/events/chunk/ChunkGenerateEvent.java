package dev.ultreon.quantum.api.events.chunk;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.gen.chunk.RecordingChunk;
import org.jetbrains.annotations.NotNull;

public class ChunkGenerateEvent extends ChunkEvent implements Cancelable {
    private final @NotNull RecordingChunk recordingChunk;
    private boolean canceled;

    public ChunkGenerateEvent(@NotNull RecordingChunk recordingChunk,
                              @NotNull BuilderChunk chunk) {
        super(chunk);
        this.recordingChunk = recordingChunk;
    }

    public @NotNull RecordingChunk getRecordingChunk() {
        return recordingChunk;
    }

    @Override
    public @NotNull BuilderChunk getChunk() {
        return (BuilderChunk) super.getChunk();
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
