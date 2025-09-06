package dev.ultreon.quantum.util;

import java.util.Objects;

@SuppressWarnings("unused")
public class Vec4 implements Cloneable {
    public float x, y, z, w;

    public Vec4(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4() {
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

    public float getW() {
        return this.w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public static Vec4 mul(Vec4 a, Vec4 b) {
        return new Vec4(a.x * b.x, a.y * b.y, a.z * b.z, a.w * b.w);
    }

    public static Vec4 div(Vec4 a, Vec4 b) {
        return new Vec4(a.x / b.x, a.y / b.y, a.z / b.z, a.w / b.w);
    }

    public static Vec4 add(Vec4 a, Vec4 b) {
        return new Vec4(a.x + b.x, a.y + b.y, a.z + b.z, a.w + b.w);
    }

    public static Vec4 sub(Vec4 a, Vec4 b) {
        return new Vec4(a.x - b.x, a.y - b.y, a.z - b.z, a.w - b.w);
    }

    public static float dot(Vec4 a, Vec4 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
    }

    public static DVec4 pow(Vec4 a, Vec4 b) {
        return new DVec4(Math.pow(a.x, b.x), Math.pow(a.y, b.y), Math.pow(a.z, b.z), Math.pow(a.w, b.w));
    }

    public float dot(Vec4 vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z + this.w * vec.w;
    }

    public float dot(float x, float y, float z, float w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    public float dot(float v) {
        return this.x * v + this.y * v + this.z * v + this.w * v;
    }

    public float len2 () {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    public Vec4 nor () {
        final float len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.mul(1f / (float)Math.sqrt(len2));
    }

    public double dst(Vec4 vec) {
        float a = vec.x - this.x;
        float b = vec.y - this.y;
        float c = vec.z - this.z;
        float d = vec.w - this.w;
        return Math.sqrt(a * a + b * b + c * c + d * d);
    }

    public double dst(float x, float y, float z, float w) {
        float a = x - this.x;
        float b = y - this.y;
        float c = z - this.z;
        float d = w - this.w;
        return Math.sqrt(a * a + b * b + c * c + d * d);
    }

    public Vec4 set(Vec4 vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        this.w = vec.w;
        return this;
    }

    public Vec4 set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vec4 set(float v) {
        this.x = v;
        this.y = v;
        this.z = v;
        this.w = v;
        return this;
    }

    public Vec4 add(Vec4 vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        this.w += vec.w;
        return this;
    }

    public Vec4 add(float x, float y, float z, float w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Vec4 add(float v) {
        this.x += v;
        this.y += v;
        this.z += v;
        this.w += v;
        return this;
    }

    public Vec4 sub(Vec4 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        this.w -= vec.w;
        return this;
    }

    public Vec4 sub(float x, float y, float z, float w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    public Vec4 sub(float v) {
        this.x -= v;
        this.y -= v;
        this.z -= v;
        this.w -= v;
        return this;
    }

    public Vec4 mul(Vec4 vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;
        this.w *= vec.w;
        return this;
    }

    public Vec4 mul(float x, float y, float z, float w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public Vec4 mul(float v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        this.w *= v;
        return this;
    }

    public Vec4 div(Vec4 vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        this.z /= vec.z;
        this.w /= vec.w;
        return this;
    }

    public Vec4 div(float x, float y, float z, float w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        return this;
    }

    public Vec4 div(float v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        this.w /= v;
        return this;
    }

    public Vec4 mod(Vec4 vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        this.z %= vec.z;
        this.w %= vec.z;
        return this;
    }

    public Vec4 mod(float x, float y, float z, float w) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        this.w %= w;
        return this;
    }

    public Vec4 mod(float v) {
        this.x %= v;
        this.y %= v;
        this.z %= v;
        this.w %= v;
        return this;
    }

    public Vec4 pow(Vec4 vec) {
        this.x = (float) Math.pow(this.x, vec.x);
        this.y = (float) Math.pow(this.y, vec.y);
        this.z = (float) Math.pow(this.z, vec.z);
        this.w = (float) Math.pow(this.w, vec.w);
        return this;
    }

    public Vec4 pow(float x, float y, float z, float w) {
        this.x = (float) Math.pow(this.x, x);
        this.y = (float) Math.pow(this.y, y);
        this.z = (float) Math.pow(this.z, z);
        this.w = (float) Math.pow(this.w, w);
        return this;
    }

    public Vec4 pow(float v) {
        this.x = (float) Math.pow(this.x, v);
        this.y = (float) Math.pow(this.y, v);
        this.z = (float) Math.pow(this.z, v);
        this.w = (float) Math.pow(this.w, v);
        return this;
    }

    public Vec4 neg() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
        return this;
    }

    public Vec4 inc() {
        this.x++;
        this.y++;
        this.z++;
        this.w++;
        return this;
    }

    public Vec4 dec() {
        this.x--;
        this.y--;
        this.z--;
        this.w--;
        return this;
    }

    public Vec4 abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        this.w = Math.abs(this.w);
        return this;
    }

    public Vec4 floor() {
        this.x = (float) Math.floor(this.x);
        this.y = (float) Math.floor(this.y);
        this.z = (float) Math.floor(this.z);
        this.w = (float) Math.floor(this.w);
        return this;
    }

    public Vec4 ceil() {
        this.x = (float) Math.ceil(this.x);
        this.y = (float) Math.ceil(this.y);
        this.z = (float) Math.ceil(this.z);
        this.w = (float) Math.ceil(this.w);
        return this;
    }

    public Vec4 cpy() {
        return new Vec4(this.x, this.y, this.z, this.w);
    }

    public DVec4 d() {
        return new DVec4(this.x, this.y, this.z, this.w);
    }

    public Vec4 f() {
        return new Vec4(this.x, this.y, this.z, this.w);
    }

    public IVec4 i() {
        return new IVec4((int) this.x, (int) this.y, (int) this.z, (int) this.w);
    }

    @Override
    public Vec4 clone() {
        try {
            Vec4 clone = (Vec4) super.clone();

            clone.x = this.x;
            clone.y = this.y;
            clone.z = this.z;
            clone.w = this.w;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Vec4 vector4i = (Vec4) o;
        return this.getX() == vector4i.getX() && this.getY() == vector4i.getY() && this.getZ() == vector4i.getZ() && this.getW() == vector4i.getW();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getX(), this.getY(), this.getZ(), this.getW());
    }

    @Override
    public String toString() {
        return String.format("%f, %f, %f, %f", this.x, this.y, this.z, this.w);
    }
}
