package com.ultreon.quantum.entity.ai;

import com.ultreon.libs.commons.v0.vector.Vec3d;

public record PathPoint(Vec3d position, Vec3d motion, Vec3d look) {
}
