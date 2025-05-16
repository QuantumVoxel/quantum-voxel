package dev.ultreon.quantum.util;

import java.util.Objects;

/**
 * A 3D Integer vector is a vector that has three dimensions, each of which is a float.
 * This vector is used in many places in the game, such as representing the position of a block in the world.
 * It is also used for other data that has three dimensions, such as the size of a block model.
 *
 * @author XyperCode
 * @see Vec2f
 * @see Vec3i
 * @see Vec3d
 */
@SuppressWarnings("unused")
public class Vec3f implements Cloneable {
    public float x, y, z;

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3f(Vec2f vec, float z) {
        this(vec.x, vec.y, z);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public Vec3f(float x, Vec2f vec) {
        this(x, vec.x, vec.y);
    }

    public Vec3f(Vec3f vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vec3f(Vec3i vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vec3f(Vec3d vec) {
        this((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public Vec3f() {

    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return this.z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public static Vec3f mul(Vec3f a, Vec3f b) {
        return new Vec3f(a.x * b.x, a.y * b.y, a.z * b.z);
    }

    public static Vec3f div(Vec3f a, Vec3f b) {
        return new Vec3f(a.x / b.x, a.y / b.y, a.z / b.z);
    }

    public static Vec3f add(Vec3f a, Vec3f b) {
        return new Vec3f(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vec3f sub(Vec3f a, Vec3f b) {
        return new Vec3f(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static float dot(Vec3f a, Vec3f b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static Vec3d pow(Vec3f a, Vec3f b) {
        return new Vec3d(Math.pow(a.x, b.x), Math.pow(a.y, b.y), Math.pow(a.z, b.z));
    }

    public float dot(Vec3f vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public float dot(float x, float y, float z) {
        return this.x * x + this.y * y + this.z * z;
    }

    public float dot(float v) {
        return this.x * v + this.y * v + this.z * v;
    }

    public float len2 () {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public Vec3f nor () {
        final float len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.mul(1f / (float)Math.sqrt(len2));
    }

    public double dst(Vec3f vec) {
        float a = vec.x - this.x;
        float b = vec.y - this.y;
        float c = vec.z - this.z;
        return Math.sqrt(a * a + b * b + c * c);
    }

    public double dst(float x, float y, float z) {
        float a = x - this.x;
        float b = y - this.y;
        float c = z - this.z;
        return Math.sqrt(a * a + b * b + c * c);
    }

    public Vec3f set(Vec3f vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        return this;
    }

    public Vec3f set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3f set(float v) {
        this.x = v;
        this.y = v;
        this.z = v;
        return this;
    }

    public Vec3f add(Vec3f vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;
    }

    public Vec3f add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vec3f add(float v) {
        this.x += v;
        this.y += v;
        this.z += v;
        return this;
    }

    public Vec3f sub(Vec3f vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        return this;
    }

    public Vec3f sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vec3f sub(float v) {
        this.x -= v;
        this.y -= v;
        this.z -= v;
        return this;
    }

    public Vec3f mul(Vec3f vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;
        return this;
    }

    public Vec3f mul(float x, float y, float z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vec3f mul(float v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        return this;
    }

    public Vec3f div(Vec3f vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        this.z /= vec.z;
        return this;
    }

    public Vec3f div(float x, float y, float z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    public Vec3f div(float v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        return this;
    }

    public Vec3f mod(Vec3f vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        this.z %= vec.z;
        return this;
    }

    public Vec3f mod(float x, float y, float z) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        return this;
    }

    public Vec3f mod(float v) {
        this.x %= v;
        this.y %= v;
        this.z %= v;
        return this;
    }

    public Vec3f pow(Vec3f vec) {
        this.x = (float) Math.pow(this.x, vec.x);
        this.y = (float) Math.pow(this.y, vec.y);
        this.z = (float) Math.pow(this.z, vec.z);
        return this;
    }

    public Vec3f pow(float x, float y, float z) {
        this.x = (float) Math.pow(this.x, x);
        this.y = (float) Math.pow(this.y, y);
        this.z = (float) Math.pow(this.z, z);
        return this;
    }

    public Vec3f pow(float v) {
        this.x = (float) Math.pow(this.x, v);
        this.y = (float) Math.pow(this.y, v);
        this.z = (float) Math.pow(this.z, v);
        return this;
    }

    public Vec3f neg() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    public Vec3f inc() {
        this.x++;
        this.y++;
        this.z++;
        return this;
    }

    public Vec3f dec() {
        this.x--;
        this.y--;
        this.z--;
        return this;
    }

    public Vec3f abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        return this;
    }

    public Vec3f floor() {
        this.x = (float) Math.floor(this.x);
        this.y = (float) Math.floor(this.y);
        this.z = (float) Math.floor(this.z);
        return this;
    }

    public Vec3f ceil() {
        this.x = (float) Math.ceil(this.x);
        this.y = (float) Math.ceil(this.y);
        this.z = (float) Math.ceil(this.z);
        return this;
    }

    public Vec3f cpy() {
        return new Vec3f(this.x, this.y, this.z);
    }

    public Vec3d d() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public Vec3f f() {
        return new Vec3f(this.x, this.y, this.z);
    }

    public Vec3i i() {
        return new Vec3i((int) this.x, (int) this.y, (int) this.z);
    }

    @Override
    public Vec3f clone() {
        try {
            Vec3f clone = (Vec3f) super.clone();

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
        Vec3f vector4i = (Vec3f) o;
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
