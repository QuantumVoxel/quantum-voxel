package dev.ultreon.quantum.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.block.state.StringSerializable;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Axis;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.util.Vec3f;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.rng.RNG;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum Direction implements StringSerializable {
    UP(new Vector3(0, 1, 0), -1, new Quaternion(Vector3.Y, 90 * MathUtils.degRad)),
    DOWN(new Vector3(0, -1, 0), -1, new Quaternion(Vector3.Y, -90 * MathUtils.degRad)),
    NORTH(new Vector3(0, 0, 1), 0, new Quaternion(Vector3.Y, 0 * MathUtils.degRad)),
    WEST(new Vector3(-1, 0, 0), 1, new Quaternion(Vector3.Y, 90 * MathUtils.degRad)),
    SOUTH(new Vector3(0, 0, -1), 2, new Quaternion(Vector3.Y, 180 * MathUtils.degRad)),
    EAST(new Vector3(1, 0, 0), 3, new Quaternion(Vector3.Y, -90 * MathUtils.degRad));

    public static final Direction[] HORIZONTAL = {NORTH, WEST, SOUTH, EAST};

    private final Vector3 normal;
    private final Quaternion rotation;
    public final int hIndex;

    Direction(Vector3 normal, int index, Quaternion rotation) {
        this.normal = normal;
        this.hIndex = index;
        this.rotation = rotation;
    }

    public static @Nullable Direction ofNormal(Vec3f normal) {
        for (Direction face : Direction.values()) {
            if (face.normal.x == normal.x && face.normal.y == normal.y && face.normal.z == normal.z) {
                return face;
            }
        }
        return null;
    }

    public static Direction fromVec3d(Vec3d direction) {
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

    public static Direction random(RNG random) {
        return Direction.values()[random.nextInt(6)];
    }

    public static Direction ofNormalNew(Vec3f set) {
        float[] comps = new float[]{set.x, set.y, set.z};
        float max = Math.max(comps[0], Math.max(comps[1], comps[2]));
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

    public Direction getOpposite() {
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

    public Direction getClockwise() {
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

    public Direction getCounterClockwise() {
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

    public Direction rotateY(int hIndex) {
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

    public Vec3i getOffset() {
        switch (this) {
            case UP:
                return new Vec3i(0, 1, 0);
            case DOWN:
                return new Vec3i(0, -1, 0);
            case WEST:
                return new Vec3i(-1, 0, 0);
            case EAST:
                return new Vec3i(1, 0, 0);
            case NORTH:
                return new Vec3i(0, 0, -1);
            case SOUTH:
                return new Vec3i(0, 0, 1);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public @NotNull String serialize() {
        return name().toLowerCase();
    }

    public int getOffsetX() {
        switch (this) {
            case UP:
            case SOUTH:
            case NORTH:
            case DOWN:
                return 0;
            case WEST:
                return -1;
            case EAST:
                return 1;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getOffsetZ() {
        switch (this) {
            case UP:
            case WEST:
            case EAST:
            case DOWN:
                return 0;
            case SOUTH:
                return -1;
            case NORTH:
                return 1;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getOffsetY() {
        switch (this) {
            case WEST:
            case EAST:
            case NORTH:
            case SOUTH:
                return 0;
            case UP:
                return 1;
            case DOWN:
                return -1;
            default:
                throw new IllegalArgumentException();
        }
    }

    public boolean isNegative() {
        return this == Direction.DOWN || this == Direction.WEST || this == Direction.SOUTH;
    }
}
