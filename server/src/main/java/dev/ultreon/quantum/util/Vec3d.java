package dev.ultreon.quantum.util;

import java.io.Externalizable;
import java.io.IOException;
import java.util.Objects;

/**
 * A 3D Integer vector is a vector that has three dimensions, each of which is a double.
 * This vector is used in many places in the game, such as representing the position of a block in the world.
 * It is also used for other data that has three dimensions, such as the size of a block model.
 *
 * @author XyperCode
 * @see Vec2d
 * @see Vec3i
 * @see Vec3f
 */
@SuppressWarnings("unused")
public class Vec3d implements Cloneable {
    public double x;
    public double y;
    public double z;

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3d(Vec2d vec, double z) {
        this(vec.x, vec.y, z);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public Vec3d(double x, Vec2d vec) {
        this(x, vec.x, vec.y);
    }

    public Vec3d(Vec3i vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vec3d(Vec3f vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vec3d(Vec3d vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vec3d(Point p) {
        this(p.getX(), p.getY(), p.getZ());
    }

    public Vec3d() {
        this(0.0, 0.0, 0.0);
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public static Vec3d scl(Vec3d a, Vec3d b) {
        return new Vec3d(a.x * b.x, a.y * b.y, a.z * b.z);
    }

    public static Vec3d div(Vec3d a, Vec3d b) {
        return new Vec3d(a.x / b.x, a.y / b.y, a.z / b.z);
    }

    public static Vec3d add(Vec3d a, Vec3d b) {
        return new Vec3d(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vec3d sub(Vec3d a, Vec3d b) {
        return new Vec3d(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static double dot(Vec3d a, Vec3d b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static Vec3d pow(Vec3d a, Vec3d b) {
        return new Vec3d(Math.pow(a.x, b.x), Math.pow(a.y, b.y), Math.pow(a.z, b.z));
    }

    public double dot(Vec3d vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public double dot(double x, double y, double z) {
        return this.x * x + this.y * y + this.z * z;
    }

    public double dot(double v) {
        return this.x * v + this.y * v + this.z * v;
    }

    public double len2 () {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public Vec3d nor () {
        final double len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.scl(1f / (float)Math.sqrt(len2));
    }

    public double dst(Vec3d vec) {
        double a = vec.x - this.x;
        double b = vec.y - this.y;
        double c = vec.z - this.z;
        return Math.sqrt(a * a + b * b + c * c);
    }

    public double dst(double x, double y, double z) {
        double a = x - this.x;
        double b = y - this.y;
        double c = z - this.z;
        return Math.sqrt(a * a + b * b + c * c);
    }

    public Vec3d set(Vec3d vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        return this;
    }

    public Vec3d set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3d set(double v) {
        this.x = v;
        this.y = v;
        this.z = v;
        return this;
    }

    public Vec3d add(Vec3d vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;
    }

    public Vec3d add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vec3d add(double v) {
        this.x += v;
        this.y += v;
        this.z += v;
        return this;
    }

    public Vec3d sub(Vec3d vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        return this;
    }

    public Vec3d sub(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vec3d sub(double v) {
        this.x -= v;
        this.y -= v;
        this.z -= v;
        return this;
    }

    public Vec3d scl(Vec3d vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;
        return this;
    }

    public Vec3d scl(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vec3d scl(double v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        return this;
    }

    public Vec3d div(Vec3d vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        this.z /= vec.z;
        return this;
    }

    public Vec3d div(double x, double y, double z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    public Vec3d div(double v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        return this;
    }

    public Vec3d mod(Vec3d vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        this.z %= vec.z;
        return this;
    }

    public Vec3d mod(double x, double y, double z) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        return this;
    }

    public Vec3d mod(double v) {
        this.x %= v;
        this.y %= v;
        this.z %= v;
        return this;
    }

    public Vec3d pow(Vec3d vec) {
        this.x = Math.pow(this.x, vec.x);
        this.y = Math.pow(this.y, vec.y);
        this.z = Math.pow(this.z, vec.z);
        return this;
    }

    public Vec3d pow(double x, double y, double z) {
        this.x = Math.pow(this.x, x);
        this.y = Math.pow(this.y, y);
        this.z = Math.pow(this.z, z);
        return this;
    }

    public Vec3d pow(double v) {
        this.x = Math.pow(this.x, v);
        this.y = Math.pow(this.y, v);
        this.z = Math.pow(this.z, v);
        return this;
    }
    
    public Vec3d neg() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    public Vec3d inc() {
        this.x++;
        this.y++;
        this.z++;
        return this;
    }

    public Vec3d dec() {
        this.x--;
        this.y--;
        this.z--;
        return this;
    }

    public Vec3d abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        return this;
    }

    public Vec3d floor() {
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        this.z = Math.floor(this.z);
        return this;
    }

    public Vec3d ceil() {
        this.x = Math.ceil(this.x);
        this.y = Math.ceil(this.y);
        this.z = Math.ceil(this.z);
        return this;
    }

    public Vec3d cpy() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public Vec3d d() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public Vec3f f() {
        return new Vec3f((float) this.x, (float) this.y, (float) this.z);
    }

    public Vec3i i() {
        return new Vec3i((int) this.x, (int) this.y, (int) this.z);
    }

    @Override
    public Vec3d clone() {
        try {
            Vec3d clone = (Vec3d) super.clone();

            clone.x = this.x;
            clone.y = this.y;
            clone.z = this.z;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Vec3d vector4i = (Vec3d) o;
        return this.getX() == vector4i.getX() && this.getY() == vector4i.getY() && this.getZ() == vector4i.getZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getX(), this.getY(), this.getZ());
    }

    @Override
    public String toString() {
        return String.format("%f, %f, %f", this.x, this.y, this.z);
    }
}
