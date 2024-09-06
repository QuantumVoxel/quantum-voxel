package dev.ultreon.quantum.world.gen;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.world.structure.Structure;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.ChunkVecSpace;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.List.copyOf;

public class StructureData {
    private final List<BoundingBox> bounds = new ArrayList<>();
    private final List<Structure> structures = new ArrayList<>();

    private final Map<ChunkVec, List<StructureInstance>> structureInstances = new HashMap<>();

    public void addStructurePoint(int x, int y, int z, Structure structure) {
        synchronized (this) {
            BlockVec center = structure.getCenter();
            BlockVec size = structure.getSize();
            BoundingBox boundingBox = new BoundingBox(x + center.x, y + center.y, z + center.z, x + center.x + size.x, y + center.y + size.y, z + center.z + size.z);
            this.bounds.add(boundingBox);
            this.structures.add(structure);

            BlockVec start = new BlockVec(boundingBox.min.i(), BlockVecSpace.WORLD);
            ChunkVec startChunk = start.chunk();

            BlockVec end = new BlockVec(boundingBox.max.i(), BlockVecSpace.WORLD);
            ChunkVec endChunk = end.chunk();

            for (int cx = startChunk.x; cx <= endChunk.x; cx++) {
                for (int cy = startChunk.y; cy <= endChunk.y; cy++) {
                    for (int cz = startChunk.z; cz <= endChunk.z; cz++) {
                        ChunkVec chunkVec = new ChunkVec(cx, cy, cz, ChunkVecSpace.WORLD);
                        structureInstances.computeIfAbsent(chunkVec, k -> new ArrayList<>()).add(new StructureInstance(
                                start,
                                structure
                        ));
                    }
                }
            }

            QuantumServer.get().addGizmo(boundingBox, new Color(1, 1, 1, 0.4f));
        }
    }

    public final @Nullable Structure getStructureAt(int x, int y, int z) {
        synchronized (this) {
            for (int i = 0; i < this.bounds.size(); i++) {
                BoundingBox box = this.bounds.get(i);
                if (box.contains(x, y, z)) {
                    return this.structures.get(i);
                }
            }
        }

        return null;
    }

    public final List<BoundingBox> getStructures() {
        return copyOf(this.bounds);
    }

    public Collection<StructureInstance> getStructuresAt(ChunkVec vec) {
        List<StructureInstance> list = this.structureInstances.get(vec);
        if (list == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableCollection(list);
    }
}
