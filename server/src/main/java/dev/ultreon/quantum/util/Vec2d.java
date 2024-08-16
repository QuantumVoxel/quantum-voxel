package dev.ultreon.quantum.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

@SuppressWarnings("unused")
public class Vec2d implements Externalizable, Cloneable {
    public double x, y;

    public Vec2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2d() {

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

    public static Vec2d mul(Vec2d a, Vec2d b) {
        return new Vec2d(a.x * b.x, a.y * b.y);
    }

    public static Vec2d div(Vec2d a, Vec2d b) {
        return new Vec2d(a.x / b.x, a.y / b.y);
    }

    public static Vec2d add(Vec2d a, Vec2d b) {
        return new Vec2d(a.x + b.x, a.y + b.y);
    }

    public static Vec2d sub(Vec2d a, Vec2d b) {
        return new Vec2d(a.x - b.x, a.y - b.y);
    }

    public static double dot(Vec2d a, Vec2d b) {
        return a.x * b.x + a.y * b.y;
    }

    public static Vec2d pow(Vec2d a, Vec2d b) {
        return new Vec2d(Math.pow(a.x, b.x), Math.pow(a.y, b.y));
    }

    public double dot(Vec2d vec) {
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

    public Vec2d nor () {
        final double len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.mul(1f / (float)Math.sqrt(len2));
    }

    public double dst(Vec2d vec) {
        double a = vec.x - this.x;
        double b = vec.y - this.y;
        return Math.sqrt(a * a + b * b);
    }

    public double dst(double x, double y) {
        double a = x - this.x;
        double b = y - this.y;
        return Math.sqrt(a * a + b * b);
    }

    public Vec2d set(Vec2d vec) {
        this.x = vec.x;
        this.y = vec.y;
        return this;
    }

    public Vec2d set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec2d set(double v) {
        this.x = v;
        this.y = v;
        return this;
    }

    public Vec2d add(Vec2d vec) {
        this.x += vec.x;
        this.y += vec.y;
        return this;
    }

    public Vec2d add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vec2d add(double v) {
        this.x += v;
        this.y += v;
        return this;
    }

    public Vec2d sub(Vec2d vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        return this;
    }

    public Vec2d sub(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vec2d sub(double v) {
        this.x -= v;
        this.y -= v;
        return this;
    }

    public Vec2d mul(Vec2d vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        return this;
    }

    public Vec2d mul(double x, double y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vec2d mul(double v) {
        this.x *= v;
        this.y *= v;
        return this;
    }

    public Vec2d div(Vec2d vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        return this;
    }

    public Vec2d div(double x, double y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public Vec2d div(double v) {
        this.x /= v;
        this.y /= v;
        return this;
    }

    public Vec2d mod(Vec2d vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        return this;
    }

    public Vec2d mod(double x, double y) {
        this.x %= x;
        this.y %= y;
        return this;
    }

    public Vec2d mod(double v) {
        this.x %= v;
        this.y %= v;
        return this;
    }

    public Vec2d pow(Vec2d vec) {
        this.x = Math.pow(this.x, vec.x);
        this.y = Math.pow(this.y, vec.y);
        return this;
    }

    public Vec2d pow(double x, double y) {
        this.x = Math.pow(this.x, x);
        this.y = Math.pow(this.y, y);
        return this;
    }

    public Vec2d pow(double v) {
        this.x = Math.pow(this.x, v);
        this.y = Math.pow(this.y, v);
        return this;
    }

    public Vec2d neg() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public Vec2d inc() {
        this.x++;
        this.y++;
        return this;
    }

    public Vec2d dec() {
        this.x--;
        this.y--;
        return this;
    }

    public Vec2d abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        return this;
    }

    public Vec2d floor() {
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        return this;
    }

    public Vec2d ceil() {
        this.x = Math.ceil(this.x);
        this.y = Math.ceil(this.y);
        return this;
    }

    public Vec2d cpy() {
        return new Vec2d(this.x, this.y);
    }

    public Vec2d d() {
        return new Vec2d(this.x, this.y);
    }

    public Vec2f f() {
        return new Vec2f((float) this.x, (float) this.y);
    }

    public Vec2i i() {
        return new Vec2i((int) this.x, (int) this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Vec2d vec2D = (Vec2d) o;
        return Double.compare(vec2D.getX(), this.getX()) == 0 && Double.compare(vec2D.getY(), this.getY()) == 0;
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
    public Vec2d clone() {
        try {
            Vec2d clone = (Vec2d) super.clone();

            clone.x = this.x;
            clone.y = this.y;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(this.x);
        out.writeDouble(this.y);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        this.x = in.readDouble();
        this.y = in.readDouble();
    }
}
