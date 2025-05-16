package dev.ultreon.quantum.util;

import java.util.Objects;

@SuppressWarnings("unused")
public class Vec4f implements Cloneable {
    public float x, y, z, w;

    public Vec4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4f() {
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

    public static Vec4f mul(Vec4f a, Vec4f b) {
        return new Vec4f(a.x * b.x, a.y * b.y, a.z * b.z, a.w * b.w);
    }

    public static Vec4f div(Vec4f a, Vec4f b) {
        return new Vec4f(a.x / b.x, a.y / b.y, a.z / b.z, a.w / b.w);
    }

    public static Vec4f add(Vec4f a, Vec4f b) {
        return new Vec4f(a.x + b.x, a.y + b.y, a.z + b.z, a.w + b.w);
    }

    public static Vec4f sub(Vec4f a, Vec4f b) {
        return new Vec4f(a.x - b.x, a.y - b.y, a.z - b.z, a.w - b.w);
    }

    public static float dot(Vec4f a, Vec4f b) {
        return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
    }

    public static Vec4d pow(Vec4f a, Vec4f b) {
        return new Vec4d(Math.pow(a.x, b.x), Math.pow(a.y, b.y), Math.pow(a.z, b.z), Math.pow(a.w, b.w));
    }

    public float dot(Vec4f vec) {
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

    public Vec4f nor () {
        final float len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.mul(1f / (float)Math.sqrt(len2));
    }

    public double dst(Vec4f vec) {
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

    public Vec4f set(Vec4f vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        this.w = vec.w;
        return this;
    }

    public Vec4f set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vec4f set(float v) {
        this.x = v;
        this.y = v;
        this.z = v;
        this.w = v;
        return this;
    }

    public Vec4f add(Vec4f vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        this.w += vec.w;
        return this;
    }

    public Vec4f add(float x, float y, float z, float w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Vec4f add(float v) {
        this.x += v;
        this.y += v;
        this.z += v;
        this.w += v;
        return this;
    }

    public Vec4f sub(Vec4f vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        this.w -= vec.w;
        return this;
    }

    public Vec4f sub(float x, float y, float z, float w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    public Vec4f sub(float v) {
        this.x -= v;
        this.y -= v;
        this.z -= v;
        this.w -= v;
        return this;
    }

    public Vec4f mul(Vec4f vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;
        this.w *= vec.w;
        return this;
    }

    public Vec4f mul(float x, float y, float z, float w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public Vec4f mul(float v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        this.w *= v;
        return this;
    }

    public Vec4f div(Vec4f vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        this.z /= vec.z;
        this.w /= vec.w;
        return this;
    }

    public Vec4f div(float x, float y, float z, float w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        return this;
    }

    public Vec4f div(float v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        this.w /= v;
        return this;
    }

    public Vec4f mod(Vec4f vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        this.z %= vec.z;
        this.w %= vec.z;
        return this;
    }

    public Vec4f mod(float x, float y, float z, float w) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        this.w %= w;
        return this;
    }

    public Vec4f mod(float v) {
        this.x %= v;
        this.y %= v;
        this.z %= v;
        this.w %= v;
        return this;
    }

    public Vec4f pow(Vec4f vec) {
        this.x = (float) Math.pow(this.x, vec.x);
        this.y = (float) Math.pow(this.y, vec.y);
        this.z = (float) Math.pow(this.z, vec.z);
        this.w = (float) Math.pow(this.w, vec.w);
        return this;
    }

    public Vec4f pow(float x, float y, float z, float w) {
        this.x = (float) Math.pow(this.x, x);
        this.y = (float) Math.pow(this.y, y);
        this.z = (float) Math.pow(this.z, z);
        this.w = (float) Math.pow(this.w, w);
        return this;
    }

    public Vec4f pow(float v) {
        this.x = (float) Math.pow(this.x, v);
        this.y = (float) Math.pow(this.y, v);
        this.z = (float) Math.pow(this.z, v);
        this.w = (float) Math.pow(this.w, v);
        return this;
    }

    public Vec4f neg() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
        return this;
    }

    public Vec4f inc() {
        this.x++;
        this.y++;
        this.z++;
        this.w++;
        return this;
    }

    public Vec4f dec() {
        this.x--;
        this.y--;
        this.z--;
        this.w--;
        return this;
    }

    public Vec4f abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        this.w = Math.abs(this.w);
        return this;
    }

    public Vec4f floor() {
        this.x = (float) Math.floor(this.x);
        this.y = (float) Math.floor(this.y);
        this.z = (float) Math.floor(this.z);
        this.w = (float) Math.floor(this.w);
        return this;
    }

    public Vec4f ceil() {
        this.x = (float) Math.ceil(this.x);
        this.y = (float) Math.ceil(this.y);
        this.z = (float) Math.ceil(this.z);
        this.w = (float) Math.ceil(this.w);
        return this;
    }

    public Vec4f cpy() {
        return new Vec4f(this.x, this.y, this.z, this.w);
    }

    public Vec4d d() {
        return new Vec4d(this.x, this.y, this.z, this.w);
    }

    public Vec4f f() {
        return new Vec4f(this.x, this.y, this.z, this.w);
    }

    public Vec4i i() {
        return new Vec4i((int) this.x, (int) this.y, (int) this.z, (int) this.w);
    }

    @Override
    public Vec4f clone() {
        try {
            Vec4f clone = (Vec4f) super.clone();

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
        Vec4f vector4i = (Vec4f) o;
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
