package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.math.MathUtils;

import java.util.Objects;

public final class Rot {
    private final float radians;

    public Rot(float radians) {
        this.radians = radians;
    }

    public float getDegrees() {
        return radians * MathUtils.radDeg;
    }

    public static Rot deg(Number num) {
        return new Rot(num.floatValue() * MathUtils.degRad);
    }

    public static Rot rad(Number num) {
        return new Rot(num.floatValue());
    }

    public float radians() {
        return radians;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Rot) obj;
        return Float.floatToIntBits(this.radians) == Float.floatToIntBits(that.radians);
    }

    @Override
    public int hashCode() {
        return Objects.hash(radians);
    }

    @Override
    public String toString() {
        return "Rot[" +
               "radians=" + radians + ']';
    }

}
