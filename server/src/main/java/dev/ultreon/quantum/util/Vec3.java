package dev.ultreon.quantum.util;

import java.util.Objects;

/**
 * A 3D Integer vector is a vector that has three dimensions, each of which is a float.
 * This vector is used in many places in the game, such as representing the position of a block in the world.
 * It is also used for other data that has three dimensions, such as the size of a block model.
 *
 * @author XyperCode
 * @see Vec2
 * @see IVec3
 * @see DVec3
 */
@SuppressWarnings("unused")
public class Vec3 implements Cloneable {
    public float x, y, z;

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(Vec2 vec, float z) {
        this(vec.x, vec.y, z);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public Vec3(float x, Vec2 vec) {
        this(x, vec.x, vec.y);
    }

    public Vec3(Vec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vec3(IVec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vec3(DVec3 vec) {
        this((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public Vec3() {

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

    public static Vec3 mul(Vec3 a, Vec3 b) {
        return new Vec3(a.x * b.x, a.y * b.y, a.z * b.z);
    }

    public static Vec3 div(Vec3 a, Vec3 b) {
        return new Vec3(a.x / b.x, a.y / b.y, a.z / b.z);
    }

    public static Vec3 add(Vec3 a, Vec3 b) {
        return new Vec3(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vec3 sub(Vec3 a, Vec3 b) {
        return new Vec3(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static float dot(Vec3 a, Vec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static DVec3 pow(Vec3 a, Vec3 b) {
        return new DVec3(Math.pow(a.x, b.x), Math.pow(a.y, b.y), Math.pow(a.z, b.z));
    }

    public float dot(Vec3 vec) {
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

    public Vec3 nor () {
        final float len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.mul(1f / (float)Math.sqrt(len2));
    }

    public double dst(Vec3 vec) {
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

    public Vec3 set(Vec3 vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        return this;
    }

    public Vec3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3 set(float v) {
        this.x = v;
        this.y = v;
        this.z = v;
        return this;
    }

    public Vec3 add(Vec3 vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;
    }

    public Vec3 add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vec3 add(float v) {
        this.x += v;
        this.y += v;
        this.z += v;
        return this;
    }

    public Vec3 sub(Vec3 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        return this;
    }

    public Vec3 sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vec3 sub(float v) {
        this.x -= v;
        this.y -= v;
        this.z -= v;
        return this;
    }

    public Vec3 mul(Vec3 vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;
        return this;
    }

    public Vec3 mul(float x, float y, float z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vec3 mul(float v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        return this;
    }

    public Vec3 div(Vec3 vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        this.z /= vec.z;
        return this;
    }

    public Vec3 div(float x, float y, float z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    public Vec3 div(float v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        return this;
    }

    public Vec3 mod(Vec3 vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        this.z %= vec.z;
        return this;
    }

    public Vec3 mod(float x, float y, float z) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        return this;
    }

    public Vec3 mod(float v) {
        this.x %= v;
        this.y %= v;
        this.z %= v;
        return this;
    }

    public Vec3 pow(Vec3 vec) {
        this.x = (float) Math.pow(this.x, vec.x);
        this.y = (float) Math.pow(this.y, vec.y);
        this.z = (float) Math.pow(this.z, vec.z);
        return this;
    }

    public Vec3 pow(float x, float y, float z) {
        this.x = (float) Math.pow(this.x, x);
        this.y = (float) Math.pow(this.y, y);
        this.z = (float) Math.pow(this.z, z);
        return this;
    }

    public Vec3 pow(float v) {
        this.x = (float) Math.pow(this.x, v);
        this.y = (float) Math.pow(this.y, v);
        this.z = (float) Math.pow(this.z, v);
        return this;
    }

    public Vec3 neg() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    public Vec3 inc() {
        this.x++;
        this.y++;
        this.z++;
        return this;
    }

    public Vec3 dec() {
        this.x--;
        this.y--;
        this.z--;
        return this;
    }

    public Vec3 abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        return this;
    }

    public Vec3 floor() {
        this.x = (float) Math.floor(this.x);
        this.y = (float) Math.floor(this.y);
        this.z = (float) Math.floor(this.z);
        return this;
    }

    public Vec3 ceil() {
        this.x = (float) Math.ceil(this.x);
        this.y = (float) Math.ceil(this.y);
        this.z = (float) Math.ceil(this.z);
        return this;
    }

    public Vec3 cpy() {
        return new Vec3(this.x, this.y, this.z);
    }

    public DVec3 d() {
        return new DVec3(this.x, this.y, this.z);
    }

    public Vec3 f() {
        return new Vec3(this.x, this.y, this.z);
    }

    public IVec3 i() {
        return new IVec3((int) this.x, (int) this.y, (int) this.z);
    }

    @Override
    public Vec3 clone() {
        try {
            Vec3 clone = (Vec3) super.clone();

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
        Vec3 vector4i = (Vec3) o;
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
