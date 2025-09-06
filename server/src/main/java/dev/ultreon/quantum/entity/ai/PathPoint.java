package dev.ultreon.quantum.entity.ai;

import dev.ultreon.quantum.util.DVec3;

import java.util.Objects;

public final class PathPoint {
    public final DVec3 position = new DVec3();
    public final DVec3 motion = new DVec3();
    public final DVec3 look = new DVec3();

    public PathPoint(DVec3 position, DVec3 motion, DVec3 look) {
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
