package dev.ultreon.quantum.util;

import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.RegionVec;

public class Vec extends DVec3 implements Point {
    public Vec(double x, double y, double z) {
        super(x, y, z);
    }

    public Vec(DVec2 vec, double z) {
        super(vec, z);
    }

    public Vec(double x, DVec2 vec) {
        super(x, vec);
    }

    public Vec(IVec3 vec) {
        super(vec);
    }

    public Vec(Vec3 vec) {
        super(vec);
    }

    public Vec(DVec3 vec) {
        super(vec);
    }

    public Vec(Point p) {
        super(p);
    }

    public Vec() {
    }

    @Override
    public double dst(Point point) {
        return super.dst(point.asVec());
    }

    @Override
    public Vec add(double v) {
        return (Vec) super.add(v);
    }

    @Override
    public Vec add(DVec3 vec) {
        return (Vec) super.add(vec);
    }

    @Override
    public Vec add(Point vec) {
        return (Vec) super.add(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public Vec add(double x, double y, double z) {
        return (Vec) super.add(x, y, z);
    }

    @Override
    public Vec sub(double v) {
        return (Vec) super.sub(v);
    }

    @Override
    public Vec sub(DVec3 vec) {
        return (Vec) super.sub(vec);
    }

    @Override
    public Vec sub(Point vec) {
        return (Vec) super.sub(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public Vec sub(double x, double y, double z) {
        return (Vec) super.sub(x, y, z);
    }

    @Override
    public Vec scl(double v) {
        return (Vec) super.scl(v);
    }

    @Override
    public Vec scl(DVec3 vec) {
        return (Vec) super.scl(vec);
    }

    @Override
    public Vec scl(Point vec) {
        return (Vec) super.scl(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public Vec scl(double x, double y, double z) {
        return (Vec) super.scl(x, y, z);
    }

    @Override
    public Vec div(double v) {
        return (Vec) super.div(v);
    }

    @Override
    public Vec div(DVec3 vec) {
        return (Vec) super.div(vec);
    }

    @Override
    public Vec div(Point vec) {
        return (Vec) super.div(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public Vec div(double x, double y, double z) {
        return (Vec) super.div(x, y, z);
    }

    @Override
    public Vec abs() {
        return (Vec) super.abs();
    }

    @Override
    public Vec neg() {
        return (Vec) super.neg();
    }

    @Override
    public Vec pow(double v) {
        return (Vec) super.pow(v);
    }

    @Override
    public Vec pow(DVec3 vec) {
        return (Vec) super.pow(vec);
    }

    public Vec pow(Point vec) {
        return (Vec) super.pow(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public Vec pow(double x, double y, double z) {
        return (Vec) super.pow(x, y, z);
    }

    @Override
    public Vec offset(int x, int y, int z) {
        return (Vec) Point.super.offset(x, y, z);
    }

    @Override
    public Vec offset(IVec3 vec) {
        return (Vec) Point.super.offset(vec);
    }

    @Override
    public Vec offset(ChunkVec vec) {
        return (Vec) Point.super.offset(vec);
    }

    @Override
    public Vec offset(RegionVec vec) {
        return (Vec) Point.super.offset(vec);
    }

    @Override
    public BlockVec asBlockVec() {
        return new BlockVec(this);
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getZ() {
        return this.z;
    }

    @Override
    public int getIntX() {
        return (int) this.x;
    }

    @Override
    public int getIntY() {
        return (int) this.y;
    }

    @Override
    public int getIntZ() {
        return (int) this.z;
    }

    @Override
    public IVec2 getVec2i() {
        return new IVec2((int) x, (int) y);
    }

    @Override
    public IVec3 getVec3i() {
        return new IVec3((int) x, (int) y, (int) z);
    }

    @Override
    public Vec set(DVec3 vec) {
        return (Vec) super.set(vec);
    }

    @Override
    public Vec set(double x, double y, double z) {
        return (Vec) super.set(x, y, z);
    }

    @Override
    public Vec set(double v) {
        return (Vec) super.set(v);
    }

    @Override
    public Vec mod(double v) {
        return (Vec) super.mod(v);
    }

    @Override
    public Vec mod(DVec3 vec) {
        return (Vec) super.mod(vec);
    }

    @Override
    public Vec mod(double x, double y, double z) {
        return (Vec) super.mod(x, y, z);
    }

    @Override
    public Vec inc() {
        return (Vec) super.inc();
    }

    @Override
    public Vec dec() {
        return (Vec) super.dec();
    }

    @Override
    public Vec floor() {
        return (Vec) super.floor();
    }

    @Override
    public Vec ceil() {
        return (Vec) super.ceil();
    }

    @Override
    public Vec cpy() {
        return (Vec) super.cpy();
    }

    @Override
    public Vec clone() {
        return new Vec(x, y, z);
    }

    @Override
    public Vec nor() {
        return (Vec) super.nor();
    }

    @Override
    public double dot(Point vec) {
        return super.dot(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public Vec asVec() {
        return this;
    }
}
