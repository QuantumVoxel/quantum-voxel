package dev.ultreon.quantum.util;

import dev.ultreon.quantum.world.vec.BlockVec;

public interface Hit {
    float getDistanceMax();

    Vec getVec();
    BlockVec getBlockVec();
    Ray getRay();

    boolean isCollide();

    float getDistance();
}
