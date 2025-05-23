package dev.ultreon.quantum.client.render.meshing;

import dev.ultreon.quantum.world.Direction;

public class FaceCull {
    public static int of(boolean top, boolean bottom, boolean front, boolean left, boolean back, boolean right) {
        return (bottom ? 1 : 0) | (top ? 2 : 0) | (front ? 4 : 0) | (left ? 8 : 0) | (back ? 16 : 0) | (right ? 32 : 0);
    }

    public static boolean culls(Direction direction, int cull) {
        if (direction == null) return false;
        return (cull & 1 << direction.getOpposite().ordinal()) != 0;
    }
}
