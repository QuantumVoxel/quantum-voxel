package dev.ultreon.quantum.world.gen;

import dev.ultreon.quantum.world.structure.BlockPoint;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class FeatureInfo {
    private final List<BlockPoint> points;
    private @Nullable Set<ChunkVec> coveringChunks = null;

    public FeatureInfo(List<BlockPoint> points) {
        this.points = points;
    }

    public List<BlockPoint> points() {
        return Collections.unmodifiableList(this.points);
    }

    public Set<ChunkVec> coveringChunks() {
        if (coveringChunks == null) {
            coveringChunks = new HashSet<>();
            for (var point : points) {
                coveringChunks.add(point.pos().chunk());
            }
        }

        return coveringChunks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FeatureInfo) obj;
        return Objects.equals(this.points, that.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points);
    }

    @Override
    public String toString() {
        return "FeatureInfo[" +
               "points=" + points + ']';
    }

}
