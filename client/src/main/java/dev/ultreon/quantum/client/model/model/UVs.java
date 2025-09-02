package dev.ultreon.quantum.client.model.model;

import java.util.Objects;

public final class UVs {
    final float x1;
    final float y1;
    final float x2;
    final float y2;

    public UVs(float x1, float y1, float x2, float y2) {
        this.x1 = x1 / 16.0F;
        this.y1 = y1 / 16.0F;
        this.x2 = x2 / 16.0F;
        this.y2 = y2 / 16.0F;
    }

    public UVs(float x1, float y1, float x2, float y2, int textureWidth, int textureHeight) {
        this.x1 = x1 / textureWidth;
        this.y1 = y1 / textureHeight;
        this.x2 = x2 / textureWidth;
        this.y2 = y2 / textureHeight;
    }

    public float x1() {
        return x1;
    }

    public float y1() {
        return y1;
    }

    public float x2() {
        return x2;
    }

    public float y2() {
        return y2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UVs) obj;
        return Float.floatToIntBits(this.x1) == Float.floatToIntBits(that.x1) &&
               Float.floatToIntBits(this.y1) == Float.floatToIntBits(that.y1) &&
               Float.floatToIntBits(this.x2) == Float.floatToIntBits(that.x2) &&
               Float.floatToIntBits(this.y2) == Float.floatToIntBits(that.y2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, y1, x2, y2);
    }

    @Override
    public String toString() {
        return "UVs[" +
               "x1=" + x1 + ", " +
               "y1=" + y1 + ", " +
               "x2=" + x2 + ", " +
               "y2=" + y2 + ']';
    }


}
