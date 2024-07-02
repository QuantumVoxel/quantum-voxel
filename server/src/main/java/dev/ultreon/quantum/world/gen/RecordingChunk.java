package dev.ultreon.quantum.world.gen;

import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.block.state.BlockData;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.ChunkAccess;
import dev.ultreon.quantum.world.ServerWorld;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RecordingChunk implements ChunkAccess {
    private final BuilderChunk chunk;
    private final Set<ServerWorld.RecordedChange> recordedChanges = new HashSet<>();

    public RecordingChunk(BuilderChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public boolean setFast(int x, int y, int z, BlockData block) {
        this.recordedChanges.add(new ServerWorld.RecordedChange(x, y, z, block));

        return true;
    }

    @Override
    public boolean set(int x, int y, int z, BlockData block) {
        this.recordedChanges.add(new ServerWorld.RecordedChange(x, y, z, block));

        return true;
    }

    @Override
    public BlockData getFast(int x, int y, int z) {
        return this.chunk.getFast(x, y, z);
    }

    @Override
    public BlockData get(int x, int y, int z) {
        return this.chunk.get(x, y, z);
    }

    Collection<ServerWorld.RecordedChange> getRecordedChanges() {
        return this.recordedChanges;
    }

    @Override
    public Vec3i getOffset() {
        return this.chunk.getOffset();
    }

    @Override
    public int getHighest(int x, int z) {
        return this.chunk.getHighest(x, z);
    }
}
