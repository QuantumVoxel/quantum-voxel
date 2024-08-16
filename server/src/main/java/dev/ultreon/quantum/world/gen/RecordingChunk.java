package dev.ultreon.quantum.world.gen;

import dev.ultreon.quantum.UnsafeApi;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.ChunkAccess;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.vec.BlockVecSpace;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RecordingChunk implements ChunkAccess {
    private final BuilderChunk chunk;
    private final Set<ServerWorld.RecordedChange> deferredChanges = new HashSet<>();

    public RecordingChunk(BuilderChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public boolean setFast(int x, int y, int z, BlockProperties block) {
        this.deferredChanges.add(new ServerWorld.RecordedChange(x, y, z, block));

        return true;
    }

    @Override
    public boolean set(int x, int y, int z, BlockProperties block) {
        this.deferredChanges.add(new ServerWorld.RecordedChange(x, y, z, block));

        return true;
    }

    @Override
    @UnsafeApi
    public BlockProperties getFast(int x, int y, int z) {
        return this.chunk.getFast(x, y, z);
    }

    @Override
    public BlockProperties get(int x, int y, int z) {
        if (this.chunk.isOutOfBounds(x, y, z)) {
            ServerWorld world = this.chunk.getWorld();
            BlockVec pos = new BlockVec(x, y, z, BlockVecSpace.WORLD);
            Chunk chunk = world.getChunkAt(pos);

            if (chunk != null) {
                return chunk.get(pos.chunkLocal());
            }

            return BlockProperties.BARRIER;
        }

        return this.chunk.get(x, y, z);
    }

    Collection<ServerWorld.RecordedChange> deferredChanges() {
        return this.deferredChanges;
    }

    @Override
    public Vec3i getOffset() {
        return this.chunk.getOffset();
    }

    @Override
    public int getHeight(int x, int z) {
        return this.chunk.getHeight(x, z);
    }

    @Override
    public BlockProperties get(BlockVec localize) {
        return get(localize.getIntX(), localize.getIntY(), localize.getIntZ());
    }

    @Override
    public boolean isDisposed() {
        return false;
    }
}
