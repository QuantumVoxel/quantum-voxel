package dev.ultreon.quantum.util;

import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.libs.commons.v0.vector.Vec3i;

public interface HitResult {
    float getDistanceMax();

    Vec3d getPosition();
    Vec3i getPos();
    Ray getRay();

    boolean isCollide();

    double getDistance();
}
