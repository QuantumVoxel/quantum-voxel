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

    public Vector3 getNormal() {
        return this.normal;
    }

    public TextObject getDisplayName() {
        return TextObject.translation("quantum.block.face." + this.name().toLowerCase(Locale.ROOT));
    }

    public Axis getAxis() {
        return switch (this) {
            case UP, DOWN -> Axis.Y;
            case WEST, EAST -> Axis.X;
            case NORTH, SOUTH -> Axis.Z;
        };
    }

    public Direction getOpposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case WEST -> EAST;
            case EAST -> WEST;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
        };
    }

    public Direction getClockwise() {
        return switch (this) {
            case UP -> UP;
            case DOWN -> DOWN;
            case WEST -> SOUTH;
            case EAST -> NORTH;
            case NORTH -> EAST;
            case SOUTH -> WEST;
        };
    }

    public Direction getCounterClockwise() {
        return switch (this) {
            case UP -> UP;
            case DOWN -> DOWN;
            case WEST -> NORTH;
            case EAST -> SOUTH;
            case NORTH -> WEST;
            case SOUTH -> EAST;
        };
    }

    public Quaternion getHorizontalRotation() {
        return switch (this) {
            case UP -> UP.rotation;
            case DOWN -> DOWN.rotation;
            case WEST -> WEST.rotation;
            case EAST -> EAST.rotation;
            case NORTH -> NORTH.rotation;
            case SOUTH -> SOUTH.rotation;
        };
    }

    public int getIndex() {
        return this.hIndex;
    }

    public Direction rotateY(int hIndex) {
        if (this.hIndex == -1) return this;

        return switch (hIndex) {
            case 0 -> this;
            case 1 -> this.getClockwise();
            case 2 -> this.getClockwise().getClockwise();
            case 3 -> this.getCounterClockwise();
            default -> throw new IllegalArgumentException();
        };
    }

    public Vec3i getOffset() {
        return switch (this) {
            case UP -> new Vec3i(0, 1, 0);
            case DOWN -> new Vec3i(0, -1, 0);
            case WEST -> new Vec3i(-1, 0, 0);
            case EAST -> new Vec3i(1, 0, 0);
            case NORTH -> new Vec3i(0, 0, -1);
            case SOUTH -> new Vec3i(0, 0, 1);
        };
    }

    @Override
    public @NotNull String serialize() {
        return name().toLowerCase();
    }

    public int getOffsetX() {
        return switch (this) {
            case UP, SOUTH, NORTH, DOWN -> 0;
            case WEST -> -1;
            case EAST -> 1;
        };
    }

    public int getOffsetZ() {
        return switch (this) {
            case UP, WEST, EAST, DOWN -> 0;
            case SOUTH -> -1;
            case NORTH -> 1;
        };
    }

    public int getOffsetY() {
        return switch (this) {
            case WEST, EAST, NORTH, SOUTH -> 0;
            case UP -> 1;
            case DOWN -> -1;
        };
    }

    public boolean isNegative() {
        return this == Direction.DOWN || this == Direction.WEST || this == Direction.SOUTH;
    }
}
