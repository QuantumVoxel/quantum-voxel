package dev.ultreon.quantum.util;

import dev.ultreon.quantum.world.vec.ChunkVec;

import java.io.Externalizable;
import java.io.IOException;
import java.util.Objects;

@SuppressWarnings("unused")
public class Vec2i implements Cloneable {
    public int x, y;

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vec2i() {

    }

    public Vec2i(ChunkVec point) {
        this(point.getIntX(), point.getIntZ());
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public static Vec2i mul(Vec2i a, Vec2i b) {
        return new Vec2i(a.x * b.x, a.y * b.y);
    }

    public static Vec2i div(Vec2i a, Vec2i b) {
        return new Vec2i(a.x / b.x, a.y / b.y);
    }

    public static Vec2i add(Vec2i a, Vec2i b) {
        return new Vec2i(a.x + b.x, a.y + b.y);
    }

    public static Vec2i sub(Vec2i a, Vec2i b) {
        return new Vec2i(a.x - b.x, a.y - b.y);
    }

    public static int dot(Vec2i a, Vec2i b) {
        return a.x * b.x + a.y * b.y;
    }

    public static Vec2d pow(Vec2i a, Vec2i b) {
        return new Vec2d(Math.pow(a.x, b.x), Math.pow(a.y, b.y));
    }

    public int dot(Vec2i vec) {
        return this.x * vec.x + this.y * vec.y;
    }

    public int dot(int x, int y) {
        return this.x * x + this.y * y;
    }

    public int dot(int v) {
        return this.x * v + this.y * v;
    }

    public double dst(Vec2i vec) {
        int a = vec.x - this.x;
        int b = vec.y - this.y;
        return Math.sqrt(a * a + b * b);
    }

    public double dst(int x, int y) {
        int a = x - this.x;
        int b = y - this.y;
        return Math.sqrt(a * a + b * b);
    }

    public Vec2i set(Vec2i vec) {
        this.x = vec.x;
        this.y = vec.y;
        return this;
    }

    public Vec2i set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec2i set(int v) {
        this.x = v;
        this.y = v;
        return this;
    }

    public Vec2i add(Vec2i vec) {
        this.x += vec.x;
        this.y += vec.y;
        return this;
    }

    public Vec2i add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vec2i add(int v) {
        this.x += v;
        this.y += v;
        return this;
    }

    public Vec2i sub(Vec2i vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        return this;
    }

    public Vec2i sub(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vec2i sub(int v) {
        this.x -= v;
        this.y -= v;
        return this;
    }

    public Vec2i mul(Vec2i vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        return this;
    }

    public Vec2i mul(int x, int y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vec2i mul(int v) {
        this.x *= v;
        this.y *= v;
        return this;
    }

    public Vec2i div(Vec2i vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        return this;
    }

    public Vec2i div(int x, int y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public Vec2i div(int v) {
        this.x /= v;
        this.y /= v;
        return this;
    }

    public Vec2i mod(Vec2i vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        return this;
    }

    public Vec2i mod(int x, int y) {
        this.x %= x;
        this.y %= y;
        return this;
    }

    public Vec2i mod(int v) {
        this.x %= v;
        this.y %= v;
        return this;
    }

    public Vec2i pow(Vec2i vec) {
        this.x = (int) Math.pow(this.x, vec.x);
        this.y = (int) Math.pow(this.y, vec.y);
        return this;
    }

    public Vec2i pow(int x, int y) {
        this.x = (int) Math.pow(this.x, x);
        this.y = (int) Math.pow(this.y, y);
        return this;
    }

    public Vec2i pow(int v) {
        this.x = (int) Math.pow(this.x, v);
        this.y = (int) Math.pow(this.y, v);
        return this;
    }

    public Vec2i neg() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public Vec2i inc() {
        this.x++;
        this.y++;
        return this;
    }

    public Vec2i dec() {
        this.x--;
        this.y--;
        return this;
    }

    public Vec2i abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        return this;
    }

    public Vec2i cpy() {
        return new Vec2i(this.x, this.y);
    }

    public Vec2d d() {
        return new Vec2d(this.x, this.y);
    }

    public Vec2f f() {
        return new Vec2f(this.x, this.y);
    }

    public Vec2i i() {
        return new Vec2i(this.x, this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Vec2i vec2I = (Vec2i) o;
        return this.getX() == vec2I.getX() && this.getY() == vec2I.getY();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getX(), this.getY());
    }

    @Override
    public String toString() {
        return String.format("%d, %d", this.x, this.y);
    }

    @Override
    public Vec2i clone() {
        try {
            Vec2i clone = (Vec2i) super.clone();

            clone.x = this.x;
            clone.y = this.y;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
