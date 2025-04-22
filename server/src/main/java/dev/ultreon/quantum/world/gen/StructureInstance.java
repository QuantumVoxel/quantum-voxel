package dev.ultreon.quantum.world.gen;

import dev.ultreon.quantum.world.ChunkAccess;
import dev.ultreon.quantum.world.structure.Structure;
import dev.ultreon.quantum.world.vec.BlockVec;

import java.util.Objects;

public final class StructureInstance {
    private final BlockVec pos;
    private final Structure structure;

    public StructureInstance(
            BlockVec pos,
            Structure structure
    ) {
        this.pos = pos;
        this.structure = structure;
    }

    public boolean contains(int x, int y, int z) {
        BlockVec size = structure.getSize();
        return x >= pos.x && x < pos.x + size.x && y >= pos.y && y < pos.y + size.y && z >= pos.z && z < pos.z + size.z;
    }

    public void placeSlice(ChunkAccess recordingChunk) {
        BlockVec center = structure.getCenter().cpy().add(pos);
        structure.placeSlice(recordingChunk, center.x, center.y, center.z);
    }

    public BlockVec pos() {
        return pos;
    }

    public Structure structure() {
        return structure;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StructureInstance) obj;
        return Objects.equals(this.pos, that.pos) &&
               Objects.equals(this.structure, that.structure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, structure);
    }

    @Override
    public String toString() {
        return "StructureInstance[" +
               "pos=" + pos + ", " +
               "structure=" + structure + ']';
    }

}
