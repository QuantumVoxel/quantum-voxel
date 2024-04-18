package com.ultreon.quantum.client.model.blockbench;

import com.ultreon.quantum.world.CubicDirection;
import com.ultreon.libs.commons.v0.vector.Vec4f;

public record BBModelFace(CubicDirection blockFace, Vec4f uv, int texture) {
}
