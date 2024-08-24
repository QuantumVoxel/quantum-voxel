package dev.ultreon.quantum.client.model.blockbench;

import dev.ultreon.quantum.util.Vec4f;
import dev.ultreon.quantum.world.CubicDirection;

import java.util.Objects;

public final class BBModelFace {
    private final CubicDirection blockFace;
    private final Vec4f uv;
    private final int texture;

    public BBModelFace(CubicDirection blockFace, Vec4f uv, int texture) {
        this.blockFace = blockFace;
        this.uv = uv;
        this.texture = texture;
    }

    public CubicDirection blockFace() {
        return blockFace;
    }

    public Vec4f uv() {
        return uv;
    }

    public int texture() {
        return texture;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBModelFace) obj;
        return Objects.equals(this.blockFace, that.blockFace) &&
               Objects.equals(this.uv, that.uv) &&
               this.texture == that.texture;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockFace, uv, texture);
    }

    @Override
    public String toString() {
        return "BBModelFace[" +
               "blockFace=" + blockFace + ", " +
               "uv=" + uv + ", " +
               "texture=" + texture + ']';
    }

}
