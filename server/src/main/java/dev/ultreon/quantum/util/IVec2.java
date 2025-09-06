package dev.ultreon.quantum.util;

import dev.ultreon.quantum.world.vec.ChunkVec;

import java.util.Objects;

@SuppressWarnings("unused")
public class IVec2 implements Cloneable {
    public int x, y;

    public IVec2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public IVec2() {

    }

    public IVec2(ChunkVec point) {
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

    public static IVec2 mul(IVec2 a, IVec2 b) {
        return new IVec2(a.x * b.x, a.y * b.y);
    }

    public static IVec2 div(IVec2 a, IVec2 b) {
        return new IVec2(a.x / b.x, a.y / b.y);
    }

    public static IVec2 add(IVec2 a, IVec2 b) {
        return new IVec2(a.x + b.x, a.y + b.y);
    }

    public static IVec2 sub(IVec2 a, IVec2 b) {
        return new IVec2(a.x - b.x, a.y - b.y);
    }

    public static int dot(IVec2 a, IVec2 b) {
        return a.x * b.x + a.y * b.y;
    }

    public static DVec2 pow(IVec2 a, IVec2 b) {
        return new DVec2(Math.pow(a.x, b.x), Math.pow(a.y, b.y));
    }

    public int dot(IVec2 vec) {
        return this.x * vec.x + this.y * vec.y;
    }

    public int dot(int x, int y) {
        return this.x * x + this.y * y;
    }

    public int dot(int v) {
        return this.x * v + this.y * v;
    }

    public double dst(IVec2 vec) {
        int a = vec.x - this.x;
        int b = vec.y - this.y;
        return Math.sqrt(a * a + b * b);
    }

    public double dst(int x, int y) {
        int a = x - this.x;
        int b = y - this.y;
        return Math.sqrt(a * a + b * b);
    }

    public IVec2 set(IVec2 vec) {
        this.x = vec.x;
        this.y = vec.y;
        return this;
    }

    public IVec2 set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public IVec2 set(int v) {
        this.x = v;
        this.y = v;
        return this;
    }

    public IVec2 add(IVec2 vec) {
        this.x += vec.x;
        this.y += vec.y;
        return this;
    }

    public IVec2 add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public IVec2 add(int v) {
        this.x += v;
        this.y += v;
        return this;
    }

    public IVec2 sub(IVec2 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        return this;
    }

    public IVec2 sub(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public IVec2 sub(int v) {
        this.x -= v;
        this.y -= v;
        return this;
    }

    public IVec2 mul(IVec2 vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        return this;
    }

    public IVec2 mul(int x, int y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public IVec2 mul(int v) {
        this.x *= v;
        this.y *= v;
        return this;
    }

    public IVec2 div(IVec2 vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        return this;
    }

    public IVec2 div(int x, int y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public IVec2 div(int v) {
        this.x /= v;
        this.y /= v;
        return this;
    }

    public IVec2 mod(IVec2 vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        return this;
    }

    public IVec2 mod(int x, int y) {
        this.x %= x;
        this.y %= y;
        return this;
    }

    public IVec2 mod(int v) {
        this.x %= v;
        this.y %= v;
        return this;
    }

    public IVec2 pow(IVec2 vec) {
        this.x = (int) Math.pow(this.x, vec.x);
        this.y = (int) Math.pow(this.y, vec.y);
        return this;
    }

    public IVec2 pow(int x, int y) {
        this.x = (int) Math.pow(this.x, x);
        this.y = (int) Math.pow(this.y, y);
        return this;
    }

    public IVec2 pow(int v) {
        this.x = (int) Math.pow(this.x, v);
        this.y = (int) Math.pow(this.y, v);
        return this;
    }

    public IVec2 neg() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public IVec2 inc() {
        this.x++;
        this.y++;
        return this;
    }

    public IVec2 dec() {
        this.x--;
        this.y--;
        return this;
    }

    public IVec2 abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        return this;
    }

    public IVec2 cpy() {
        return new IVec2(this.x, this.y);
    }

    public DVec2 d() {
        return new DVec2(this.x, this.y);
    }

    public Vec2 f() {
        return new Vec2(this.x, this.y);
    }

    public IVec2 i() {
        return new IVec2(this.x, this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        IVec2 vec2I = (IVec2) o;
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
    public IVec2 clone() {
        try {
            IVec2 clone = (IVec2) super.clone();

            clone.x = this.x;
            clone.y = this.y;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
