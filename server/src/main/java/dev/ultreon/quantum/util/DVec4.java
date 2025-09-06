package dev.ultreon.quantum.util;

import java.util.Objects;

@SuppressWarnings("unused")
public class DVec4 implements Cloneable {
    public double x, y, z, w;

    public DVec4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public DVec4() {

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

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getW() {
        return this.w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public static DVec4 mul(DVec4 a, DVec4 b) {
        return new DVec4(a.x * b.x, a.y * b.y, a.z * b.z, a.w * b.w);
    }

    public static DVec4 div(DVec4 a, DVec4 b) {
        return new DVec4(a.x / b.x, a.y / b.y, a.z / b.z, a.w / b.w);
    }

    public static DVec4 add(DVec4 a, DVec4 b) {
        return new DVec4(a.x + b.x, a.y + b.y, a.z + b.z, a.w + b.w);
    }

    public static DVec4 sub(DVec4 a, DVec4 b) {
        return new DVec4(a.x - b.x, a.y - b.y, a.z - b.z, a.w - b.w);
    }

    public static double dot(DVec4 a, DVec4 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
    }

    public static DVec4 pow(DVec4 a, DVec4 b) {
        return new DVec4(Math.pow(a.x, b.x), Math.pow(a.y, b.y), Math.pow(a.z, b.z), Math.pow(a.w, b.w));
    }

    public double dot(DVec4 vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z + this.w * vec.w;
    }

    public double dot(double x, double y, double z, double w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    public double dot(double v) {
        return this.x * v + this.y * v + this.z * v + this.w * v;
    }

    public double len2 () {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    public DVec4 nor () {
        final double len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.mul(1f / (float)Math.sqrt(len2));
    }

    public double dst(DVec4 vec) {
        double a = vec.x - this.x;
        double b = vec.y - this.y;
        double c = vec.z - this.z;
        double d = vec.w - this.w;
        return Math.sqrt(a * a + b * b + c * c + d * d);
    }

    public double dst(double x, double y, double z, double w) {
        double a = x - this.x;
        double b = y - this.y;
        double c = z - this.z;
        double d = w - this.w;
        return Math.sqrt(a * a + b * b + c * c + d * d);
    }

    public DVec4 set(DVec4 vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        this.w = vec.w;
        return this;
    }

    public DVec4 set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public DVec4 set(double v) {
        this.x = v;
        this.y = v;
        this.z = v;
        this.w = v;
        return this;
    }

    public DVec4 add(DVec4 vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        this.w += vec.w;
        return this;
    }

    public DVec4 add(double x, double y, double z, double w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public DVec4 add(double v) {
        this.x += v;
        this.y += v;
        this.z += v;
        this.w += v;
        return this;
    }

    public DVec4 sub(DVec4 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        this.w -= vec.w;
        return this;
    }

    public DVec4 sub(double x, double y, double z, double w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    public DVec4 sub(double v) {
        this.x -= v;
        this.y -= v;
        this.z -= v;
        this.w -= v;
        return this;
    }

    public DVec4 mul(DVec4 vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;
        this.w *= vec.w;
        return this;
    }

    public DVec4 mul(double x, double y, double z, double w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public DVec4 mul(double v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        this.w *= v;
        return this;
    }

    public DVec4 div(DVec4 vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        this.z /= vec.z;
        this.w /= vec.w;
        return this;
    }

    public DVec4 div(double x, double y, double z, double w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        return this;
    }

    public DVec4 div(double v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        this.w /= v;
        return this;
    }

    public DVec4 mod(DVec4 vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        this.z %= vec.z;
        this.w %= vec.z;
        return this;
    }

    public DVec4 mod(double x, double y, double z, double w) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        this.w %= w;
        return this;
    }

    public DVec4 mod(double v) {
        this.x %= v;
        this.y %= v;
        this.z %= v;
        this.w %= v;
        return this;
    }

    public DVec4 pow(DVec4 vec) {
        this.x = Math.pow(this.x, vec.x);
        this.y = Math.pow(this.y, vec.y);
        this.z = Math.pow(this.z, vec.z);
        this.w = Math.pow(this.w, vec.w);
        return this;
    }

    public DVec4 pow(double x, double y, double z, double w) {
        this.x = Math.pow(this.x, x);
        this.y = Math.pow(this.y, y);
        this.z = Math.pow(this.z, z);
        this.w = Math.pow(this.w, w);
        return this;
    }

    public DVec4 pow(double v) {
        this.x = Math.pow(this.x, v);
        this.y = Math.pow(this.y, v);
        this.z = Math.pow(this.z, v);
        this.w = Math.pow(this.w, v);
        return this;
    }

    public DVec4 neg() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
        return this;
    }

    public DVec4 inc() {
        this.x++;
        this.y++;
        this.z++;
        this.w++;
        return this;
    }

    public DVec4 dec() {
        this.x--;
        this.y--;
        this.z--;
        this.w--;
        return this;
    }

    public DVec4 abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        this.w = Math.abs(this.w);
        return this;
    }

    public DVec4 floor() {
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        this.z = Math.floor(this.z);
        this.w = Math.floor(this.w);
        return this;
    }

    public DVec4 ceil() {
        this.x = Math.ceil(this.x);
        this.y = Math.ceil(this.y);
        this.z = Math.ceil(this.z);
        this.w = Math.ceil(this.w);
        return this;
    }

    public DVec4 cpy() {
        return new DVec4(this.x, this.y, this.z, this.w);
    }

    public DVec4 d() {
        return new DVec4(this.x, this.y, this.z, this.w);
    }

    public Vec4 f() {
        return new Vec4((float) this.x, (float) this.y, (float) this.z, (float) this.w);
    }

    public IVec4 i() {
        return new IVec4((int) this.x, (int) this.y, (int) this.z, (int) this.w);
    }

    @Override
    public DVec4 clone() {
        try {
            DVec4 clone = (DVec4) super.clone();

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
        DVec4 vector4i = (DVec4) o;
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
