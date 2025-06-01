package dev.ultreon.quantum.client.model.model;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.quantum.util.Axis;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents a rotational transformation applied to an element, defined by its origin, axis,
 * angle of rotation, and an optional rescaling flag.
 * <p>
 * This class is used to manage rotation information for elements in a 3D space, where the
 * rotation is specified with respect to a defined origin vector and a chosen axis (X, Y, or Z).
 * The amount of rotation is determined by an angle measured in degrees, and optionally, the
 * transformation can apply rescaling to maintain proportionality.
 * <p>
 * Instances of this class are immutable and provide methods to retrieve the properties of the rotation.
 */
public final class ElementRotation {
    public static final ElementRotation ZERO = new ElementRotation(Vector3.Zero, Axis.X, 0f, false);
    final Vector3 originVec;
    final Axis axis;
    final float angle;
    final boolean rescale;

    /**
     * @param originVec the origin vector representing the point around which the element is rotated
     * @param axis      the axis of rotation (X, Y, or Z)
     * @param angle     the angle of rotation in degrees
     * @param rescale   whether to apply rescaling after the rotation
     */
    public ElementRotation(Vector3 originVec, Axis axis, float angle, boolean rescale) {
        this.originVec = originVec;
        this.axis = axis;
        this.angle = angle;
        this.rescale = rescale;
    }

    public static ElementRotation deserialize(@Nullable JsonValue rotation) {
        if (rotation == null) {
            return new ElementRotation(new Vector3(0, 0, 0), Axis.Y, 0, false);
        }

        float[] origin = rotation.get("origin").asFloatArray();
        String axis = rotation.get("axis").asString();
        float angle = rotation.get("angle").asFloat();
        JsonValue rescale1 = rotation.get("rescale");
        boolean rescale = rescale1 == null || rescale1.asBoolean();

        Vector3 originVec = new Vector3(origin[0], origin[1], origin[2]);
        return new ElementRotation(originVec, Axis.valueOf(axis.toUpperCase(Locale.ROOT)), angle, rescale);
    }

    @Override
    public @NotNull String toString() {
        return "ElementRotation[" +
               "originVec=" + originVec + ", " +
               "axis=" + axis + ", " +
               "angle=" + angle + ", " +
               "rescale=" + rescale + ']';
    }

    public Vector3 originVec() {
        return originVec;
    }

    public Axis axis() {
        return axis;
    }

    public float angle() {
        return angle;
    }

    public boolean rescale() {
        return rescale;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ElementRotation) obj;
        return Objects.equals(this.originVec, that.originVec) &&
               Objects.equals(this.axis, that.axis) &&
               Float.floatToIntBits(this.angle) == Float.floatToIntBits(that.angle) &&
               this.rescale == that.rescale;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originVec, axis, angle, rescale);
    }


}
