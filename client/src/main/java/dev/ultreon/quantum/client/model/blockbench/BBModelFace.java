package dev.ultreon.quantum.client.model.blockbench;

import dev.ultreon.quantum.util.Vec4f;
import dev.ultreon.quantum.world.CubicDirection;

import java.util.Objects;

public record BBModelFace(CubicDirection blockFace, Vec4f uv, int texture) {

    @Override
    public String toString() {
        return "BBModelFace[" +
               "blockFace=" + blockFace + ", " +
               "uv=" + uv + ", " +
               "texture=" + texture + ']';
    }

}
