package dev.ultreon.quantum.entity.ai;

import dev.ultreon.libs.commons.v0.vector.Vec3d;

import java.util.Collections;
import java.util.List;

public record Path(List<Vec3d> points, PathPoint start, Vec3d end) {

    @Override
    public List<Vec3d> points() {
        return Collections.unmodifiableList(points);
    }

    public boolean isDone(PathPoint current) {
        return start.position().equals(end) || points.isEmpty() || current.position().equals(end);
    }

    @Override
    public String toString() {
        return "Path[" +
                "points=" + points + ", " +
                "start=" + start + ", " +
                "end=" + end + ']';
    }

}
