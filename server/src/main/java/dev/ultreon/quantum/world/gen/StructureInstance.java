package dev.ultreon.quantum.world.gen;

import dev.ultreon.quantum.world.ChunkAccess;
import dev.ultreon.quantum.world.structure.Structure;
import dev.ultreon.quantum.world.vec.BlockVec;

public record StructureInstance(
        BlockVec pos,
        Structure structure
) {

    public boolean contains(int x, int y, int z) {
        BlockVec size = structure.getSize();
        return x >= pos.x && x < pos.x + size.x && y >= pos.y && y < pos.y + size.y && z >= pos.z && z < pos.z + size.z;
    }

    public void placeSlice(ChunkAccess recordingChunk) {
        BlockVec center = structure.getCenter().cpy().add(pos);
        structure.placeSlice(recordingChunk, center.x, center.y, center.z);
    }
}
