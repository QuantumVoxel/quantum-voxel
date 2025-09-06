package dev.ultreon.quantum.util;

import java.util.Objects;

@SuppressWarnings("unused")
public class IVec4 implements Cloneable {
    public int x, y, z, w;

    public IVec4(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public IVec4() {

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

    public static IVec4 mul(IVec4 a, IVec4 b) {
        return new IVec4(a.x * b.x, a.y * b.y, a.z * b.z, a.w * b.w);
    }

    public static IVec4 div(IVec4 a, IVec4 b) {
        return new IVec4(a.x / b.x, a.y / b.y, a.z / b.z, a.w / b.w);
    }

    public static IVec4 add(IVec4 a, IVec4 b) {
        return new IVec4(a.x + b.x, a.y + b.y, a.z + b.z, a.w + b.w);
    }

    public static IVec4 sub(IVec4 a, IVec4 b) {
        return new IVec4(a.x - b.x, a.y - b.y, a.z - b.z, a.w - b.w);
    }

    public static int dot(IVec4 a, IVec4 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
    }

    public static DVec4 pow(IVec4 a, IVec4 b) {
        return new DVec4(Math.pow(a.x, b.x), Math.pow(a.y, b.y), Math.pow(a.z, b.z), Math.pow(a.w, b.w));
    }

    public int dot(IVec4 vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z + this.w * vec.w;
    }

    public int dot(int x, int y, int z, int w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    public int dot(int v) {
        return this.x * v + this.y * v + this.z * v + this.w * v;
    }

    public double dst(IVec4 vec) {
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

    public IVec4 set(IVec4 vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        this.w = vec.w;
        return this;
    }

    public IVec4 set(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public IVec4 set(int v) {
        this.x = v;
        this.y = v;
        this.z = v;
        this.w = v;
        return this;
    }

    public IVec4 add(IVec4 vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        this.w += vec.w;
        return this;
    }

    public IVec4 add(int x, int y, int z, int w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public IVec4 add(int v) {
        this.x += v;
        this.y += v;
        this.z += v;
        this.w += v;
        return this;
    }

    public IVec4 sub(IVec4 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        this.w -= vec.w;
        return this;
    }

    public IVec4 sub(int x, int y, int z, int w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    public IVec4 sub(int v) {
        this.x -= v;
        this.y -= v;
        this.z -= v;
        this.w -= v;
        return this;
    }

    public IVec4 mul(IVec4 vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;
        this.w *= vec.w;
        return this;
    }

    public IVec4 mul(int x, int y, int z, int w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public IVec4 mul(int v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        this.w *= v;
        return this;
    }

    public IVec4 div(IVec4 vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        this.z /= vec.z;
        this.w /= vec.w;
        return this;
    }

    public IVec4 div(int x, int y, int z, int w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        return this;
    }

    public IVec4 div(int v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        this.w /= v;
        return this;
    }

    public IVec4 mod(IVec4 vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        this.z %= vec.z;
        this.w %= vec.z;
        return this;
    }

    public IVec4 mod(int x, int y, int z, int w) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        this.w %= w;
        return this;
    }

    public IVec4 mod(int v) {
        this.x %= v;
        this.y %= v;
        this.z %= v;
        this.w %= v;
        return this;
    }

    public IVec4 pow(IVec4 vec) {
        this.x = (int) Math.pow(this.x, vec.x);
        this.y = (int) Math.pow(this.y, vec.y);
        this.z = (int) Math.pow(this.z, vec.z);
        this.w = (int) Math.pow(this.w, vec.w);
        return this;
    }

    public IVec4 pow(int x, int y, int z, int w) {
        this.x = (int) Math.pow(this.x, x);
        this.y = (int) Math.pow(this.y, y);
        this.z = (int) Math.pow(this.z, z);
        this.w = (int) Math.pow(this.w, w);
        return this;
    }

    public IVec4 pow(int v) {
        this.x = (int) Math.pow(this.x, v);
        this.y = (int) Math.pow(this.y, v);
        this.z = (int) Math.pow(this.z, v);
        this.w = (int) Math.pow(this.w, v);
        return this;
    }

    public IVec4 neg() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
        return this;
    }

    public IVec4 inc() {
        this.x++;
        this.y++;
        this.z++;
        this.w++;
        return this;
    }

    public IVec4 dec() {
        this.x--;
        this.y--;
        this.z--;
        this.w--;
        return this;
    }

    public IVec4 abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        this.w = Math.abs(this.w);
        return this;
    }

    public IVec4 cpy() {
        return new IVec4(this.x, this.y, this.z, this.w);
    }

    public DVec4 d() {
        return new DVec4(this.x, this.y, this.z, this.w);
    }

    public Vec4 f() {
        return new Vec4(this.x, this.y, this.z, this.w);
    }

    public IVec4 i() {
        return new IVec4(this.x, this.y, this.z, this.w);
    }

    @Override
    public IVec4 clone() {
        try {
            IVec4 clone = (IVec4) super.clone();

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
        IVec4 vec4I = (IVec4) o;
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
