package dev.ultreon.quantum.util;

import java.util.Objects;

@SuppressWarnings("unused")
public class Vec2 implements Cloneable {
    public float x, y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2() {

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

    public static Vec2 mul(Vec2 a, Vec2 b) {
        return new Vec2(a.x * b.x, a.y * b.y);
    }

    public static Vec2 div(Vec2 a, Vec2 b) {
        return new Vec2(a.x / b.x, a.y / b.y);
    }

    public static Vec2 add(Vec2 a, Vec2 b) {
        return new Vec2(a.x + b.x, a.y + b.y);
    }

    public static Vec2 sub(Vec2 a, Vec2 b) {
        return new Vec2(a.x - b.x, a.y - b.y);
    }

    public static float dot(Vec2 a, Vec2 b) {
        return a.x * b.x + a.y * b.y;
    }

    public static DVec2 pow(Vec2 a, Vec2 b) {
        return new DVec2(Math.pow(a.x, b.x), Math.pow(a.y, b.y));
    }

    public float dot(Vec2 vec) {
        return this.x * vec.x + this.y * vec.y;
    }

    public float dot(float x, float y) {
        return this.x * x + this.y * y;
    }

    public float dot(float v) {
        return this.x * v + this.y * v;
    }

    public float len2 () {
        return this.x * this.x + this.y * this.y;
    }

    public Vec2 nor () {
        final float len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.mul(1f / (float)Math.sqrt(len2));
    }

    public double dst(Vec2 vec) {
        float a = vec.x - this.x;
        float b = vec.y - this.y;
        return Math.sqrt(a * a + b * b);
    }

    public double dst(float x, float y) {
        float a = x - this.x;
        float b = y - this.y;
        return Math.sqrt(a * a + b * b);
    }

    public Vec2 set(Vec2 vec) {
        this.x = vec.x;
        this.y = vec.y;
        return this;
    }

    public Vec2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec2 set(float v) {
        this.x = v;
        this.y = v;
        return this;
    }

    public Vec2 add(Vec2 vec) {
        this.x += vec.x;
        this.y += vec.y;
        return this;
    }

    public Vec2 add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vec2 add(float v) {
        this.x += v;
        this.y += v;
        return this;
    }

    public Vec2 sub(Vec2 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        return this;
    }

    public Vec2 sub(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vec2 sub(float v) {
        this.x -= v;
        this.y -= v;
        return this;
    }

    public Vec2 mul(Vec2 vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        return this;
    }

    public Vec2 mul(float x, float y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vec2 mul(float v) {
        this.x *= v;
        this.y *= v;
        return this;
    }

    public Vec2 div(Vec2 vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        return this;
    }

    public Vec2 div(float x, float y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public Vec2 div(float v) {
        this.x /= v;
        this.y /= v;
        return this;
    }

    public Vec2 mod(Vec2 vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        return this;
    }

    public Vec2 mod(float x, float y) {
        this.x %= x;
        this.y %= y;
        return this;
    }

    public Vec2 mod(float v) {
        this.x %= v;
        this.y %= v;
        return this;
    }

    public Vec2 pow(Vec2 vec) {
        this.x = (float) Math.pow(this.x, vec.x);
        this.y = (float) Math.pow(this.y, vec.y);
        return this;
    }

    public Vec2 pow(float x, float y) {
        this.x = (float) Math.pow(this.x, x);
        this.y = (float) Math.pow(this.y, y);
        return this;
    }

    public Vec2 pow(float v) {
        this.x = (float) Math.pow(this.x, v);
        this.y = (float) Math.pow(this.y, v);
        return this;
    }

    public Vec2 neg() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public Vec2 inc() {
        this.x++;
        this.y++;
        return this;
    }

    public Vec2 dec() {
        this.x--;
        this.y--;
        return this;
    }

    public Vec2 abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        return this;
    }

    public Vec2 floor() {
        this.x = (float) Math.floor(this.x);
        this.y = (float) Math.floor(this.y);
        return this;
    }

    public Vec2 ceil() {
        this.x = (float) Math.ceil(this.x);
        this.y = (float) Math.ceil(this.y);
        return this;
    }

    public Vec2 cpy() {
        return new Vec2(this.x, this.y);
    }

    public DVec2 d() {
        return new DVec2(this.x, this.y);
    }

    public Vec2 f() {
        return new Vec2(this.x, this.y);
    }

    public IVec2 i() {
        return new IVec2((int) this.x, (int) this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Vec2 vec2 = (Vec2) o;
        return Float.compare(vec2.getX(), this.getX()) == 0 && Float.compare(vec2.getY(), this.getY()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getX(), this.getY());
    }

    @Override
    public String toString() {
        return String.format("%f, %f", this.x, this.y);
    }

    @Override
    public Vec2 clone() {
        try {
            Vec2 clone = (Vec2) super.clone();

            clone.x = this.x;
            clone.y = this.y;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
