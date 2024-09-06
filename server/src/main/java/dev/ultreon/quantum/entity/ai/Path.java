package dev.ultreon.quantum.entity.ai;

import dev.ultreon.quantum.util.Vec3d;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Path {
    private final List<Vec3d> points;
    private final PathPoint start;
    private final Vec3d end;

    public Path(List<Vec3d> points, PathPoint start, Vec3d end) {
        this.points = points;
        this.start = start;
        this.end = end;
    }

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

    public PathPoint start() {
        return start;
    }

    public Vec3d end() {
        return end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Path) obj;
        return Objects.equals(this.points, that.points) &&
               Objects.equals(this.start, that.start) &&
               Objects.equals(this.end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, start, end);
    }


}
