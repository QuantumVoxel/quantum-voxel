package dev.ultreon.quantum.world.vec;

import dev.ultreon.quantum.util.Point;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.util.Vec3f;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.ubo.types.MapType;

import java.util.Objects;

public class SectionVec extends Vec3i implements Cloneable {
    public SectionVec(int x, int y, int z) {
        super(x, y, z);
    }

    public SectionVec(Vec3i vec) {
        super(vec);
    }

    public SectionVec(MapType data) {
        this(data.getInt("x"), data.getInt("y"), data.getInt("z"));
    }

    public SectionVec() {
        this(0, 0, 0);
    }

    public MapType save(MapType data) {
        data.putInt("x", this.x);
        data.putInt("y", this.y);
        data.putInt("z", this.z);
        return data;
    }

    @Override
    public SectionVec add(int x, int y, int z) {
        return new SectionVec(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public SectionVec add(Vec3i vec) {
        return new SectionVec(this.x + vec.x, this.y + vec.y, this.z + vec.z);
    }

    public SectionVec add(SectionVec vec) {
        return new SectionVec(this.x + vec.x, this.y + vec.y, this.z + vec.z);
    }

    @Override
    public SectionVec sub(int x, int y, int z) {
        return new SectionVec(this.x - x, this.y - y, this.z - z);
    }

    @Override
    public SectionVec sub(Vec3i vec) {
        return new SectionVec(this.x - vec.x, this.y - vec.y, this.z - vec.z);
    }

    public SectionVec sub(SectionVec vec) {
        return new SectionVec(this.x - vec.x, this.y - vec.y, this.z - vec.z);
    }

    @Override
    public SectionVec mul(int x, int y, int z) {
        return new SectionVec(this.x * x, this.y * y, this.z * z);
    }

    @Override
    public SectionVec mul(Vec3i vec) {
        return new SectionVec(this.x * vec.x, this.y * vec.y, this.z * vec.z);
    }

    public SectionVec mul(SectionVec vec) {
        return new SectionVec(this.x * vec.x, this.y * vec.y, this.z * vec.z);
    }

    @Override
    public SectionVec div(int x, int y, int z) {
        return new SectionVec(this.x / x, this.y / y, this.z / z);
    }

    @Override
    public SectionVec div(Vec3i vec) {
        return new SectionVec(this.x / vec.x, this.y / vec.y, this.z / vec.z);
    }

    public SectionVec div(SectionVec vec) {
        return new SectionVec(this.x / vec.x, this.y / vec.y, this.z / vec.z);
    }

    @Override
    public SectionVec mod(int x, int y, int z) {
        return new SectionVec(this.x % x, this.y % y, this.z % z);
    }

    @Override
    public SectionVec mod(Vec3i vec) {
        return new SectionVec(this.x % vec.x, this.y % vec.y, this.z % vec.z);
    }

    public SectionVec mod(SectionVec vec) {
        return new SectionVec(this.x % vec.x, this.y % vec.y, this.z % vec.z);
    }

    @Override
    public SectionVec abs() {
        return new SectionVec(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));
    }

    @Override
    public SectionVec neg() {
        return new SectionVec(-this.x, -this.y, -this.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SectionVec that = (SectionVec) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "%d,%d,%d".formatted(this.x, this.y, this.z);
    }

    @Override
    public SectionVec clone() {
        return (SectionVec) super.clone();
    }

    @Override
    public SectionVec cpy() {
        return new SectionVec(this.x, this.y, this.z);
    }

    @Override
    public SectionVec add(int v) {
        return new SectionVec(this.x + v, this.y + v, this.z + v);
    }

    @Override
    public SectionVec sub(int v) {
        return new SectionVec(this.x - v, this.y - v, this.z - v);
    }

    @Override
    public SectionVec mul(int v) {
        return new SectionVec(this.x * v, this.y * v, this.z * v);
    }

    @Override
    public SectionVec div(int v) {
        return new SectionVec(this.x / v, this.y / v, this.z / v);
    }

    @Override
    public SectionVec mod(int v) {
        return new SectionVec(this.x % v, this.y % v, this.z % v);
    }

    public int dot(SectionVec vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public SectionVec cross(SectionVec vec) {
        return new SectionVec(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z, this.x * vec.y - this.y * vec.x);
    }

    public SectionVec max(SectionVec vec) {
        return new SectionVec(Math.max(this.x, vec.x), Math.max(this.y, vec.y), Math.max(this.z, vec.z));
    }

    public SectionVec min(SectionVec vec) {
        return new SectionVec(Math.min(this.x, vec.x), Math.min(this.y, vec.y), Math.min(this.z, vec.z));
    }

    @Override
    public SectionVec dec() {
        return new SectionVec(this.x - 1, this.y - 1, this.z - 1);
    }

    @Override
    public SectionVec inc() {
        return new SectionVec(this.x + 1, this.y + 1, this.z + 1);
    }

    public SectionVec pow(int v) {
        return new SectionVec((int) Math.pow(this.x, v), (int) Math.pow(this.y, v), (int) Math.pow(this.z, v));
    }

    public SectionVec sqrt() {
        return new SectionVec((int) Math.sqrt(this.x), (int) Math.sqrt(this.y), (int) Math.sqrt(this.z));
    }

    @Override
    public SectionVec pow(Vec3i vec) {
        return new SectionVec((int) Math.pow(this.x, vec.x), (int) Math.pow(this.y, vec.y), (int) Math.pow(this.z, vec.z));
    }

    public SectionVec pow(Vec3f vec) {
        return new SectionVec((int) Math.pow(this.x, vec.x), (int) Math.pow(this.y, vec.y), (int) Math.pow(this.z, vec.z));
    }

    public SectionVec pow(Vec3d vec) {
        return new SectionVec((int) Math.pow(this.x, vec.x), (int) Math.pow(this.y, vec.y), (int) Math.pow(this.z, vec.z));
    }

    public SectionVec max(Vec3i vec) {
        return new SectionVec(Math.max(this.x, vec.x), Math.max(this.y, vec.y), Math.max(this.z, vec.z));
    }

    public SectionVec min(Vec3i vec) {
        return new SectionVec(Math.min(this.x, vec.x), Math.min(this.y, vec.y), Math.min(this.z, vec.z));
    }

    public SectionVec pow(double x, double y, double z) {
        return new SectionVec((int) Math.pow(this.x, x), (int) Math.pow(this.y, y), (int) Math.pow(this.z, z));
    }

    public SectionVec max(int x, int y, int z) {
        return new SectionVec(Math.max(this.x, x), Math.max(this.y, y), Math.max(this.z, z));
    }

    public SectionVec min(int x, int y, int z) {
        return new SectionVec(Math.min(this.x, x), Math.min(this.y, y), Math.min(this.z, z));
    }

    public SectionVec pow(Point point) {
        return new SectionVec((int) Math.pow(this.x, point.getX()), (int) Math.pow(this.y, point.getY()), (int) Math.pow(this.z, point.getZ()));
    }

    public SectionVec max(Point point) {
        return new SectionVec((int) Math.max(this.x, point.getX()), (int) Math.max(this.y, point.getY()), (int) Math.max(this.z, point.getZ()));
    }

    public SectionVec min(Point point) {
        return new SectionVec((int) Math.min(this.x, point.getX()), (int) Math.min(this.y, point.getY()), (int) Math.min(this.z, point.getZ()));
    }

    public SectionVec pow(double v) {
        return new SectionVec((int) Math.pow(this.x, v), (int) Math.pow(this.y, v), (int) Math.pow(this.z, v));
    }

    public SectionVec max(int v) {
        return new SectionVec(Math.max(this.x, v), Math.max(this.y, v), Math.max(this.z, v));
    }

    public SectionVec min(int v) {
        return new SectionVec(Math.min(this.x, v), Math.min(this.y, v), Math.min(this.z, v));
    }
}
