package dev.ultreon.quantum.client.render.meshing;

import dev.ultreon.quantum.world.Direction;

public class Light {
    public static long of(byte top, byte bottom, byte front, byte left, byte back, byte right) {
        return top | (bottom << 8) | (front << 16) | (left << 24) | ((long) back << 32) | ((long) right << 48);
    }

    public static byte get(long light, Direction direction) {
        return (byte) (light & 1 << direction.ordinal() * 8);
    }
}
