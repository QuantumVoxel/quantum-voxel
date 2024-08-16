package dev.ultreon.quantum.util;

import dev.ultreon.quantum.world.vec.BlockVec;

public interface HitResult {
    float getDistanceMax();

    Vec getVec();
    BlockVec getBlockVec();
    Ray getRay();

    boolean isCollide();

    double getDistance();
}
