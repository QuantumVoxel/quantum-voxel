package dev.ultreon.quantum.client.model.blockbench;

import dev.ultreon.libs.commons.v0.vector.Vec4f;
import dev.ultreon.quantum.world.CubicDirection;

public record BBModelFace(CubicDirection blockFace, Vec4f uv, int texture) {
}
