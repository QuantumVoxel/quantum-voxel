package dev.ultreon.quantum.world.gen.chunk;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.util.Point;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * The RecordingChunk class is an implementation of the ChunkAccess interface.
 * It wraps a BuilderChunk and provides functionality to record changes to the chunk.
 * Those recorded changes can then be used in another chunk to load it in.
 */
@Deprecated
public class RecordingChunk implements ChunkAccess {
    private final BuilderChunk chunk;
    private final Set<ServerWorld.RecordedChange> deferredChanges = new HashSet<>();

    public RecordingChunk(BuilderChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public boolean set(int x, int y, int z, BlockState block) {
        this.deferredChanges.add(new ServerWorld.RecordedChange(x, y, z, block));

        return true;
    }

    @Override
    public boolean set(Point pos, BlockState block) {
        this.deferredChanges.add(new ServerWorld.RecordedChange(pos.getIntX(), pos.getIntY(), pos.getIntZ(), block));
        return false;
    }

    @Override
    public BlockState get(int x, int y, int z) {
        if (this.chunk.isOutOfBounds(x, y, z)) {
            ServerWorld world = this.chunk.getWorld();
            BlockVec pos = new BlockVec(x, y, z, BlockVecSpace.WORLD);
            Chunk chunk = world.getChunkAtNoLoad(pos);

            if (chunk != null) {
                return chunk.get((Point) pos.chunkLocal());
            }

            return BlockState.BARRIER;
        }

        return this.chunk.get(x, y, z);
    }

    public Collection<ServerWorld.RecordedChange> deferredChanges() {
        return this.deferredChanges;
    }

    @Override
    public BlockVec getOffset() {
        return this.chunk.getOffset();
    }

    @Override
    @Deprecated
    public int getHeight(int x, int z) {
        return this.chunk.getHeight(x, z, HeightmapType.WORLD_SURFACE);
    }

    @Override
    public BlockState get(Point vec) {
        return get(vec.getIntX(), vec.getIntY(), vec.getIntZ());
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public ServerWorld getWorld() {
        return chunk.getWorld();
    }

    public ChunkVec getVec() {
        return chunk.getVec();
    }
}
