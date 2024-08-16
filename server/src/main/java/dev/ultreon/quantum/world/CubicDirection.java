package dev.ultreon.quantum.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.libs.commons.v0.vector.Vec3f;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Axis;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum CubicDirection {
    UP(new Vector3(0, 1, 0), -1, new Quaternion(Vector3.Y, 90 * MathUtils.degRad)),
    DOWN(new Vector3(0, -1, 0), -1, new Quaternion(Vector3.Y, -90 * MathUtils.degRad)),
    NORTH(new Vector3(0, 0, 1), 0, new Quaternion(Vector3.Y, 0 * MathUtils.degRad)),
    WEST(new Vector3(-1, 0, 0), 1, new Quaternion(Vector3.Y, 90 * MathUtils.degRad)),
    SOUTH(new Vector3(0, 0, -1), 2, new Quaternion(Vector3.Y, 180 * MathUtils.degRad)),
    EAST(new Vector3(1, 0, 0), 3, new Quaternion(Vector3.Y, -90 * MathUtils.degRad));

    public static final CubicDirection[] HORIZONTAL = {NORTH, WEST, SOUTH, EAST};

    private final Vector3 normal;
    private final Quaternion rotation;
    public final int hIndex;

    CubicDirection(Vector3 normal, int index, Quaternion rotation) {
        this.normal = normal;
        this.hIndex = index;
        this.rotation = rotation;
    }

    public static @Nullable CubicDirection ofNormal(Vec3f normal) {
        for (CubicDirection face : CubicDirection.values()) {
            if (face.normal.x == normal.x && face.normal.y == normal.y && face.normal.z == normal.z) {
                return face;
            }
        }
        return null;
    }

    public static CubicDirection fromVec3d(Vec3d direction) {
        double[] comps = new double[]{direction.x, direction.y, direction.z};
        double max;

        if (comps[0] > comps[1]) {
            max = Math.max(comps[0], comps[2]);
        } else {
            max = Math.max(comps[1], comps[2]);
        }

        if (max == comps[0]) return max < 0 ? SOUTH : NORTH;
        else if (max == comps[1]) return max < 0 ? DOWN : UP;
        else return max < 0 ? WEST : EAST;
    }

    public Vector3 getNormal() {
        return this.normal;
    }

    public TextObject getDisplayName() {
        return TextObject.translation("quantum.block.face." + this.name().toLowerCase(Locale.ROOT));
    }

    public Axis getAxis() {
        switch (this) {
            case UP:
            case DOWN:
                return Axis.Y;
            case WEST:
            case EAST:
                return Axis.X;
            case NORTH:
            case SOUTH:
                return Axis.Z;
            default:
                throw new IllegalArgumentException();
        }
    }

    public CubicDirection getOpposite() {
        switch (this) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case WEST:
                return EAST;
            case EAST:
                return WEST;
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            default:
                throw new IllegalArgumentException();
        }
    }

    public CubicDirection getClockwise() {
        switch (this) {
            case UP:
                return UP;
            case DOWN:
                return DOWN;
            case WEST:
                return SOUTH;
            case EAST:
                return NORTH;
            case NORTH:
                return EAST;
            case SOUTH:
                return WEST;
            default:
                throw new IllegalArgumentException();
        }
    }

    public CubicDirection getCounterClockwise() {
        switch (this) {
            case UP:
                return UP;
            case DOWN:
                return DOWN;
            case WEST:
                return NORTH;
            case EAST:
                return SOUTH;
            case NORTH:
                return WEST;
            case SOUTH:
                return EAST;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Quaternion getHorizontalRotation() {
        switch (this) {
            case UP:
                return UP.rotation;
            case DOWN:
                return DOWN.rotation;
            case WEST:
                return WEST.rotation;
            case EAST:
                return EAST.rotation;
            case NORTH:
                return NORTH.rotation;
            case SOUTH:
                return SOUTH.rotation;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getIndex() {
        return this.hIndex;
    }

    public CubicDirection rotateY(int hIndex) {
        if (this.hIndex == -1) return this;

        switch (hIndex) {
            case 0:
                return this;
            case 1:
                return this.getClockwise();
            case 2:
                return this.getClockwise().getClockwise();
            case 3:
                return this.getCounterClockwise();
            default:
                throw new IllegalArgumentException();
        }
    }

    public BlockVec getOffset() {
        switch (this) {
            case UP:
                return new BlockVec(0, 1, 0);
            case DOWN:
                return new BlockVec(0, -1, 0);
            case WEST:
                return new BlockVec(-1, 0, 0);
            case EAST:
                return new BlockVec(1, 0, 0);
            case NORTH:
                return new BlockVec(0, 0, -1);
            case SOUTH:
                return new BlockVec(0, 0, 1);
            default:
                throw new IllegalArgumentException();
        }
    }
}
