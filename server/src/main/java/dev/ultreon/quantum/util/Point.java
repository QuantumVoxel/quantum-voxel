package dev.ultreon.quantum.util;

import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.*;

public interface Point {
    double getX();

    double getY();

    double getZ();

    default int getIntX() {
        return (int) getX();
    }

    default int getIntY() {
        return (int) getY();
    }

    default int getIntZ() {
        return (int) getZ();
    }

    default Vec3i getVec3i() {
        return new Vec3i(getIntX(), getIntY(), getIntZ());
    }

    default Vec2i getVec2i() {
        return new Vec2i(getIntX(), getIntZ());
    }

    default ChunkVec chunk() {
        return new ChunkVec(getIntX() / World.CS, getIntY() / World.CS, getIntZ() / World.CS, ChunkVecSpace.WORLD);
    }

    default RegionVec region() {
        return new RegionVec(getIntX() / World.CS / World.REGION_SIZE, getIntY() / World.CS / World.REGION_SIZE, getIntZ() / World.CS / World.REGION_SIZE);
    }

    Point add(Point vec);

    Point sub(Point vec);

    Point scl(Point vec);

    Point div(Point vec);

    default Point offset(int x, int y, int z) {
        return new Vec(x, y, z);
    }

    default Point offset(Vec3i vec) {
        return offset(vec.x, vec.y, vec.z);
    }

    default Point offset(ChunkVec vec) {
        return offset(vec.x * World.CS, vec.y * World.CS, vec.z * World.CS);
    }

    default Point offset(RegionVec vec) {
        return offset(vec.x * World.CS * World.REGION_SIZE, vec.y * World.CS * World.REGION_SIZE, vec.z * World.CS * World.REGION_SIZE);
    }

    double dst(Point point);

    double dot(Point vec);

    default Vec asVec() {
        return new Vec(getX(), getY(), getZ());
    }

    default BlockVec asBlockVec() {
        return new BlockVec(getIntX(), getIntY(), getIntZ());
    }

    default boolean equals(int i, int i1, int i2) {
        return i == getIntX() && i1 == getIntY() && i2 == getIntZ();
    }

    default boolean equals(double x, double y, double z) {
        return x == getX() && y == getY() && z == getZ();
    }
}
