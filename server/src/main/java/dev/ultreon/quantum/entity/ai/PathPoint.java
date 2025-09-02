package dev.ultreon.quantum.entity.ai;

import dev.ultreon.quantum.util.Vec3d;

import java.util.Objects;

public final class PathPoint {
    public final Vec3d position = new Vec3d();
    public final Vec3d motion = new Vec3d();
    public final Vec3d look = new Vec3d();

    public PathPoint(Vec3d position, Vec3d motion, Vec3d look) {
        this.position.set(position);
        this.motion.set(motion);
        this.look.set(look);
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
