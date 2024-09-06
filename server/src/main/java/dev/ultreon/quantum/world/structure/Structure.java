package dev.ultreon.quantum.world.structure;

import dev.ultreon.quantum.world.BlockSetter;
import dev.ultreon.quantum.world.ChunkAccess;
import dev.ultreon.quantum.world.vec.BlockVec;

public abstract class Structure {
    protected Structure() {}

    public abstract void place(BlockSetter setter, int x, int y, int z);

    public abstract BlockVec getSize();

    public abstract BlockVec getCenter();

    public final void placeSlice(ChunkAccess recordingChunk, int worldX, int worldY, int worldZ) {
        WorldSlice worldSlice = new WorldSlice(recordingChunk);
        place(worldSlice, worldX, worldY, worldZ);
    }

    public boolean contains(int x, int y, int z) {
        BlockVec size = getSize();
        BlockVec start = getCenter().cpy().sub(size.cpy().div(2));

        return x >= start.x && x < start.x + size.x && y >= start.y && y < start.y + size.y && z >= start.z && z < start.z + size.z;
    }
}
