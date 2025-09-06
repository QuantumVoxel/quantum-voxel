package dev.ultreon.quantum.util;

import java.util.Objects;

@SuppressWarnings("unused")
public class DVec2 implements Cloneable {
    public double x, y;

    public DVec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public DVec2() {

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

    public static DVec2 mul(DVec2 a, DVec2 b) {
        return new DVec2(a.x * b.x, a.y * b.y);
    }

    public static DVec2 div(DVec2 a, DVec2 b) {
        return new DVec2(a.x / b.x, a.y / b.y);
    }

    public static DVec2 add(DVec2 a, DVec2 b) {
        return new DVec2(a.x + b.x, a.y + b.y);
    }

    public static DVec2 sub(DVec2 a, DVec2 b) {
        return new DVec2(a.x - b.x, a.y - b.y);
    }

    public static double dot(DVec2 a, DVec2 b) {
        return a.x * b.x + a.y * b.y;
    }

    public static DVec2 pow(DVec2 a, DVec2 b) {
        return new DVec2(Math.pow(a.x, b.x), Math.pow(a.y, b.y));
    }

    public double dot(DVec2 vec) {
        return this.x * vec.x + this.y * vec.y;
    }

    public double dot(double x, double y) {
        return this.x * x + this.y * y;
    }

    public double dot(double v) {
        return this.x * v + this.y * v;
    }

    public double len2 () {
        return this.x * this.x + this.y * this.y;
    }

    public DVec2 nor () {
        final double len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.mul(1f / (float)Math.sqrt(len2));
    }

    public double dst(DVec2 vec) {
        double a = vec.x - this.x;
        double b = vec.y - this.y;
        return Math.sqrt(a * a + b * b);
    }

    public double dst(double x, double y) {
        double a = x - this.x;
        double b = y - this.y;
        return Math.sqrt(a * a + b * b);
    }

    public DVec2 set(DVec2 vec) {
        this.x = vec.x;
        this.y = vec.y;
        return this;
    }

    public DVec2 set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public DVec2 set(double v) {
        this.x = v;
        this.y = v;
        return this;
    }

    public DVec2 add(DVec2 vec) {
        this.x += vec.x;
        this.y += vec.y;
        return this;
    }

    public DVec2 add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public DVec2 add(double v) {
        this.x += v;
        this.y += v;
        return this;
    }

    public DVec2 sub(DVec2 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        return this;
    }

    public DVec2 sub(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public DVec2 sub(double v) {
        this.x -= v;
        this.y -= v;
        return this;
    }

    public DVec2 mul(DVec2 vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        return this;
    }

    public DVec2 mul(double x, double y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public DVec2 mul(double v) {
        this.x *= v;
        this.y *= v;
        return this;
    }

    public DVec2 div(DVec2 vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        return this;
    }

    public DVec2 div(double x, double y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public DVec2 div(double v) {
        this.x /= v;
        this.y /= v;
        return this;
    }

    public DVec2 mod(DVec2 vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        return this;
    }

    public DVec2 mod(double x, double y) {
        this.x %= x;
        this.y %= y;
        return this;
    }

    public DVec2 mod(double v) {
        this.x %= v;
        this.y %= v;
        return this;
    }

    public DVec2 pow(DVec2 vec) {
        this.x = Math.pow(this.x, vec.x);
        this.y = Math.pow(this.y, vec.y);
        return this;
    }

    public DVec2 pow(double x, double y) {
        this.x = Math.pow(this.x, x);
        this.y = Math.pow(this.y, y);
        return this;
    }

    public DVec2 pow(double v) {
        this.x = Math.pow(this.x, v);
        this.y = Math.pow(this.y, v);
        return this;
    }

    public DVec2 neg() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public DVec2 inc() {
        this.x++;
        this.y++;
        return this;
    }

    public DVec2 dec() {
        this.x--;
        this.y--;
        return this;
    }

    public DVec2 abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        return this;
    }

    public DVec2 floor() {
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        return this;
    }

    public DVec2 ceil() {
        this.x = Math.ceil(this.x);
        this.y = Math.ceil(this.y);
        return this;
    }

    public DVec2 cpy() {
        return new DVec2(this.x, this.y);
    }

    public DVec2 d() {
        return new DVec2(this.x, this.y);
    }

    public Vec2 f() {
        return new Vec2((float) this.x, (float) this.y);
    }

    public IVec2 i() {
        return new IVec2((int) this.x, (int) this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        DVec2 DVec2 = (DVec2) o;
        return Double.compare(DVec2.getX(), this.getX()) == 0 && Double.compare(DVec2.getY(), this.getY()) == 0;
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
    public DVec2 clone() {
        try {
            DVec2 clone = (DVec2) super.clone();

            clone.x = this.x;
            clone.y = this.y;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
