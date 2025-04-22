package dev.ultreon.quantum.util;

import java.io.Externalizable;
import java.io.IOException;
import java.util.Objects;

@SuppressWarnings("unused")
public class Vec2f implements Cloneable {
    public float x, y;

    public Vec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2f() {

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

    public static Vec2f mul(Vec2f a, Vec2f b) {
        return new Vec2f(a.x * b.x, a.y * b.y);
    }

    public static Vec2f div(Vec2f a, Vec2f b) {
        return new Vec2f(a.x / b.x, a.y / b.y);
    }

    public static Vec2f add(Vec2f a, Vec2f b) {
        return new Vec2f(a.x + b.x, a.y + b.y);
    }

    public static Vec2f sub(Vec2f a, Vec2f b) {
        return new Vec2f(a.x - b.x, a.y - b.y);
    }

    public static float dot(Vec2f a, Vec2f b) {
        return a.x * b.x + a.y * b.y;
    }

    public static Vec2d pow(Vec2f a, Vec2f b) {
        return new Vec2d(Math.pow(a.x, b.x), Math.pow(a.y, b.y));
    }

    public float dot(Vec2f vec) {
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

    public Vec2f nor () {
        final float len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.mul(1f / (float)Math.sqrt(len2));
    }

    public double dst(Vec2f vec) {
        float a = vec.x - this.x;
        float b = vec.y - this.y;
        return Math.sqrt(a * a + b * b);
    }

    public double dst(float x, float y) {
        float a = x - this.x;
        float b = y - this.y;
        return Math.sqrt(a * a + b * b);
    }

    public Vec2f set(Vec2f vec) {
        this.x = vec.x;
        this.y = vec.y;
        return this;
    }

    public Vec2f set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec2f set(float v) {
        this.x = v;
        this.y = v;
        return this;
    }

    public Vec2f add(Vec2f vec) {
        this.x += vec.x;
        this.y += vec.y;
        return this;
    }

    public Vec2f add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vec2f add(float v) {
        this.x += v;
        this.y += v;
        return this;
    }

    public Vec2f sub(Vec2f vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        return this;
    }

    public Vec2f sub(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vec2f sub(float v) {
        this.x -= v;
        this.y -= v;
        return this;
    }

    public Vec2f mul(Vec2f vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        return this;
    }

    public Vec2f mul(float x, float y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vec2f mul(float v) {
        this.x *= v;
        this.y *= v;
        return this;
    }

    public Vec2f div(Vec2f vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        return this;
    }

    public Vec2f div(float x, float y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public Vec2f div(float v) {
        this.x /= v;
        this.y /= v;
        return this;
    }

    public Vec2f mod(Vec2f vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        return this;
    }

    public Vec2f mod(float x, float y) {
        this.x %= x;
        this.y %= y;
        return this;
    }

    public Vec2f mod(float v) {
        this.x %= v;
        this.y %= v;
        return this;
    }

    public Vec2f pow(Vec2f vec) {
        this.x = (float) Math.pow(this.x, vec.x);
        this.y = (float) Math.pow(this.y, vec.y);
        return this;
    }

    public Vec2f pow(float x, float y) {
        this.x = (float) Math.pow(this.x, x);
        this.y = (float) Math.pow(this.y, y);
        return this;
    }

    public Vec2f pow(float v) {
        this.x = (float) Math.pow(this.x, v);
        this.y = (float) Math.pow(this.y, v);
        return this;
    }

    public Vec2f neg() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public Vec2f inc() {
        this.x++;
        this.y++;
        return this;
    }

    public Vec2f dec() {
        this.x--;
        this.y--;
        return this;
    }

    public Vec2f abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        return this;
    }

    public Vec2f floor() {
        this.x = (float) Math.floor(this.x);
        this.y = (float) Math.floor(this.y);
        return this;
    }

    public Vec2f ceil() {
        this.x = (float) Math.ceil(this.x);
        this.y = (float) Math.ceil(this.y);
        return this;
    }

    public Vec2f cpy() {
        return new Vec2f(this.x, this.y);
    }

    public Vec2d d() {
        return new Vec2d(this.x, this.y);
    }

    public Vec2f f() {
        return new Vec2f(this.x, this.y);
    }

    public Vec2i i() {
        return new Vec2i((int) this.x, (int) this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Vec2f vec2F = (Vec2f) o;
        return Float.compare(vec2F.getX(), this.getX()) == 0 && Float.compare(vec2F.getY(), this.getY()) == 0;
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
    public Vec2f clone() {
        try {
            Vec2f clone = (Vec2f) super.clone();

            clone.x = this.x;
            clone.y = this.y;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
