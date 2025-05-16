package dev.ultreon.quantum.util;

import java.util.Objects;

@SuppressWarnings("unused")
public class Vec4i implements Cloneable {
    public int x, y, z, w;

    public Vec4i(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4i() {

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

    public int getZ() {
        return this.z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getW() {
        return this.w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public static Vec4i mul(Vec4i a, Vec4i b) {
        return new Vec4i(a.x * b.x, a.y * b.y, a.z * b.z, a.w * b.w);
    }

    public static Vec4i div(Vec4i a, Vec4i b) {
        return new Vec4i(a.x / b.x, a.y / b.y, a.z / b.z, a.w / b.w);
    }

    public static Vec4i add(Vec4i a, Vec4i b) {
        return new Vec4i(a.x + b.x, a.y + b.y, a.z + b.z, a.w + b.w);
    }

    public static Vec4i sub(Vec4i a, Vec4i b) {
        return new Vec4i(a.x - b.x, a.y - b.y, a.z - b.z, a.w - b.w);
    }

    public static int dot(Vec4i a, Vec4i b) {
        return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
    }

    public static Vec4d pow(Vec4i a, Vec4i b) {
        return new Vec4d(Math.pow(a.x, b.x), Math.pow(a.y, b.y), Math.pow(a.z, b.z), Math.pow(a.w, b.w));
    }

    public int dot(Vec4i vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z + this.w * vec.w;
    }

    public int dot(int x, int y, int z, int w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    public int dot(int v) {
        return this.x * v + this.y * v + this.z * v + this.w * v;
    }

    public double dst(Vec4i vec) {
        int a = vec.x - this.x;
        int b = vec.y - this.y;
        int c = vec.z - this.z;
        int d = vec.w - this.w;
        return Math.sqrt(a * a + b * b + c * c + d * d);
    }

    public double dst(int x, int y, int z, int w) {
        int a = x - this.x;
        int b = y - this.y;
        int c = z - this.z;
        int d = w - this.w;
        return Math.sqrt(a * a + b * b + c * c + d * d);
    }

    public Vec4i set(Vec4i vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        this.w = vec.w;
        return this;
    }

    public Vec4i set(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vec4i set(int v) {
        this.x = v;
        this.y = v;
        this.z = v;
        this.w = v;
        return this;
    }

    public Vec4i add(Vec4i vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        this.w += vec.w;
        return this;
    }

    public Vec4i add(int x, int y, int z, int w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Vec4i add(int v) {
        this.x += v;
        this.y += v;
        this.z += v;
        this.w += v;
        return this;
    }

    public Vec4i sub(Vec4i vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        this.w -= vec.w;
        return this;
    }

    public Vec4i sub(int x, int y, int z, int w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    public Vec4i sub(int v) {
        this.x -= v;
        this.y -= v;
        this.z -= v;
        this.w -= v;
        return this;
    }

    public Vec4i mul(Vec4i vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;
        this.w *= vec.w;
        return this;
    }

    public Vec4i mul(int x, int y, int z, int w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public Vec4i mul(int v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        this.w *= v;
        return this;
    }

    public Vec4i div(Vec4i vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        this.z /= vec.z;
        this.w /= vec.w;
        return this;
    }

    public Vec4i div(int x, int y, int z, int w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        return this;
    }

    public Vec4i div(int v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        this.w /= v;
        return this;
    }

    public Vec4i mod(Vec4i vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        this.z %= vec.z;
        this.w %= vec.z;
        return this;
    }

    public Vec4i mod(int x, int y, int z, int w) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        this.w %= w;
        return this;
    }

    public Vec4i mod(int v) {
        this.x %= v;
        this.y %= v;
        this.z %= v;
        this.w %= v;
        return this;
    }

    public Vec4i pow(Vec4i vec) {
        this.x = (int) Math.pow(this.x, vec.x);
        this.y = (int) Math.pow(this.y, vec.y);
        this.z = (int) Math.pow(this.z, vec.z);
        this.w = (int) Math.pow(this.w, vec.w);
        return this;
    }

    public Vec4i pow(int x, int y, int z, int w) {
        this.x = (int) Math.pow(this.x, x);
        this.y = (int) Math.pow(this.y, y);
        this.z = (int) Math.pow(this.z, z);
        this.w = (int) Math.pow(this.w, w);
        return this;
    }

    public Vec4i pow(int v) {
        this.x = (int) Math.pow(this.x, v);
        this.y = (int) Math.pow(this.y, v);
        this.z = (int) Math.pow(this.z, v);
        this.w = (int) Math.pow(this.w, v);
        return this;
    }

    public Vec4i neg() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
        return this;
    }

    public Vec4i inc() {
        this.x++;
        this.y++;
        this.z++;
        this.w++;
        return this;
    }

    public Vec4i dec() {
        this.x--;
        this.y--;
        this.z--;
        this.w--;
        return this;
    }

    public Vec4i abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        this.w = Math.abs(this.w);
        return this;
    }

    public Vec4i cpy() {
        return new Vec4i(this.x, this.y, this.z, this.w);
    }

    public Vec4d d() {
        return new Vec4d(this.x, this.y, this.z, this.w);
    }

    public Vec4f f() {
        return new Vec4f(this.x, this.y, this.z, this.w);
    }

    public Vec4i i() {
        return new Vec4i(this.x, this.y, this.z, this.w);
    }

    @Override
    public Vec4i clone() {
        try {
            Vec4i clone = (Vec4i) super.clone();

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
        Vec4i vec4I = (Vec4i) o;
        return this.getX() == vec4I.getX() && this.getY() == vec4I.getY() && this.getZ() == vec4I.getZ() && this.getW() == vec4I.getW();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getX(), this.getY(), this.getZ(), this.getW());
    }

    @Override
    public String toString() {
        return String.format("%d, %d, %d, %d", this.x, this.y, this.z, this.w);
    }
}
