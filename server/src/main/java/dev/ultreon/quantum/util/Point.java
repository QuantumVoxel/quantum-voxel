package dev.ultreon.quantum.util;

import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.RegionVec;

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
        return new ChunkVec(getIntX() / World.CHUNK_SIZE, getIntY() / World.CHUNK_HEIGHT, getIntZ() / World.CHUNK_SIZE);
    }

    default RegionVec region() {
        return new RegionVec(getIntX() / World.CHUNK_SIZE / World.REGION_SIZE, getIntZ() / World.CHUNK_SIZE / World.REGION_SIZE);
    }

    default Point offset(int x, int y, int z) {
        return new Vec(x, y, z);
    }

    default Point offset(Vec3i vec) {
        return offset(vec.x, vec.y, vec.z);
    }

    default Point offset(Vec2i vec) {
        return offset(vec.x, 0, vec.y);
    }

    default Point offset(int x, int z) {
        return offset(x, 0, z);
    }

    default Point offset(ChunkVec vec) {
        return offset(vec.x * World.CHUNK_SIZE, vec.y * World.CHUNK_HEIGHT, vec.z * World.CHUNK_SIZE);
    }

    default Point offset(RegionVec vec) {
        return offset(vec.x * World.CHUNK_SIZE * World.REGION_SIZE, vec.z * World.CHUNK_SIZE * World.REGION_SIZE);
    }

    double dst(Point point);

    default Vec asVec() {
        return new Vec(getX(), getY(), getZ());
    }

    default BlockVec asBlockVec() {
        return new BlockVec(getIntX(), getIntY(), getIntZ(), BlockVecSpace.WORLD);
    }
}
