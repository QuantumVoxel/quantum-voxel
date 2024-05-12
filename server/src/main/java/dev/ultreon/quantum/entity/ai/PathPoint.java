package dev.ultreon.quantum.entity.ai;

import dev.ultreon.libs.commons.v0.vector.Vec3d;

import java.util.Objects;

public final class PathPoint {
    private final Vec3d position;
    private final Vec3d motion;
    private final Vec3d look;

    public PathPoint(Vec3d position, Vec3d motion, Vec3d look) {
        this.position = position;
        this.motion = motion;
        this.look = look;
    }

    public Vec3d position() {
        return position;
    }

    public Vec3d motion() {
        return motion;
    }

    public Vec3d look() {
        return look;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PathPoint) obj;
        return Objects.equals(this.position, that.position) &&
               Objects.equals(this.motion, that.motion) &&
               Objects.equals(this.look, that.look);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, motion, look);
    }

    @Override
    public String toString() {
        return "PathPoint[" +
               "position=" + position + ", " +
               "motion=" + motion + ", " +
               "look=" + look + ']';
    }

}
