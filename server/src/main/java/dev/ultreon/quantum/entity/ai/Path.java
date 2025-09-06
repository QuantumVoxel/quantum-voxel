package dev.ultreon.quantum.entity.ai;

import dev.ultreon.quantum.util.DVec3;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Path {
    private final List<DVec3> points;
    private final PathPoint start;
    private final DVec3 end;

    public Path(List<DVec3> points, PathPoint start, DVec3 end) {
        this.points = points;
        this.start = start;
        this.end = end;
    }

    public List<DVec3> points() {
        return Collections.unmodifiableList(points);
    }

    public boolean isDone(PathPoint current) {
        return start.position.equals(end) || points.isEmpty() || current.position.equals(end);
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

    public DVec3 end() {
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
