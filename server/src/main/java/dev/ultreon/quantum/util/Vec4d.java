package dev.ultreon.quantum.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

@SuppressWarnings("unused")
public class Vec4d implements Externalizable, Cloneable {
    public double x, y, z, w;

    public Vec4d(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4d() {

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

    public static Vec4d mul(Vec4d a, Vec4d b) {
        return new Vec4d(a.x * b.x, a.y * b.y, a.z * b.z, a.w * b.w);
    }

    public static Vec4d div(Vec4d a, Vec4d b) {
        return new Vec4d(a.x / b.x, a.y / b.y, a.z / b.z, a.w / b.w);
    }

    public static Vec4d add(Vec4d a, Vec4d b) {
        return new Vec4d(a.x + b.x, a.y + b.y, a.z + b.z, a.w + b.w);
    }

    public static Vec4d sub(Vec4d a, Vec4d b) {
        return new Vec4d(a.x - b.x, a.y - b.y, a.z - b.z, a.w - b.w);
    }

    public static double dot(Vec4d a, Vec4d b) {
        return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
    }

    public static Vec4d pow(Vec4d a, Vec4d b) {
        return new Vec4d(Math.pow(a.x, b.x), Math.pow(a.y, b.y), Math.pow(a.z, b.z), Math.pow(a.w, b.w));
    }

    public double dot(Vec4d vec) {
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

    public Vec4d nor () {
        final double len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.mul(1f / (float)Math.sqrt(len2));
    }

    public double dst(Vec4d vec) {
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

    public Vec4d set(Vec4d vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        this.w = vec.w;
        return this;
    }

    public Vec4d set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vec4d set(double v) {
        this.x = v;
        this.y = v;
        this.z = v;
        this.w = v;
        return this;
    }

    public Vec4d add(Vec4d vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        this.w += vec.w;
        return this;
    }

    public Vec4d add(double x, double y, double z, double w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Vec4d add(double v) {
        this.x += v;
        this.y += v;
        this.z += v;
        this.w += v;
        return this;
    }

    public Vec4d sub(Vec4d vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        this.w -= vec.w;
        return this;
    }

    public Vec4d sub(double x, double y, double z, double w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    public Vec4d sub(double v) {
        this.x -= v;
        this.y -= v;
        this.z -= v;
        this.w -= v;
        return this;
    }

    public Vec4d mul(Vec4d vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;
        this.w *= vec.w;
        return this;
    }

    public Vec4d mul(double x, double y, double z, double w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public Vec4d mul(double v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        this.w *= v;
        return this;
    }

    public Vec4d div(Vec4d vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        this.z /= vec.z;
        this.w /= vec.w;
        return this;
    }

    public Vec4d div(double x, double y, double z, double w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        return this;
    }

    public Vec4d div(double v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        this.w /= v;
        return this;
    }

    public Vec4d mod(Vec4d vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        this.z %= vec.z;
        this.w %= vec.z;
        return this;
    }

    public Vec4d mod(double x, double y, double z, double w) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        this.w %= w;
        return this;
    }

    public Vec4d mod(double v) {
        this.x %= v;
        this.y %= v;
        this.z %= v;
        this.w %= v;
        return this;
    }

    public Vec4d pow(Vec4d vec) {
        this.x = Math.pow(this.x, vec.x);
        this.y = Math.pow(this.y, vec.y);
        this.z = Math.pow(this.z, vec.z);
        this.w = Math.pow(this.w, vec.w);
        return this;
    }

    public Vec4d pow(double x, double y, double z, double w) {
        this.x = Math.pow(this.x, x);
        this.y = Math.pow(this.y, y);
        this.z = Math.pow(this.z, z);
        this.w = Math.pow(this.w, w);
        return this;
    }

    public Vec4d pow(double v) {
        this.x = Math.pow(this.x, v);
        this.y = Math.pow(this.y, v);
        this.z = Math.pow(this.z, v);
        this.w = Math.pow(this.w, v);
        return this;
    }

    public Vec4d neg() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
        return this;
    }

    public Vec4d inc() {
        this.x++;
        this.y++;
        this.z++;
        this.w++;
        return this;
    }

    public Vec4d dec() {
        this.x--;
        this.y--;
        this.z--;
        this.w--;
        return this;
    }

    public Vec4d abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        this.w = Math.abs(this.w);
        return this;
    }

    public Vec4d floor() {
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        this.z = Math.floor(this.z);
        this.w = Math.floor(this.w);
        return this;
    }

    public Vec4d ceil() {
        this.x = Math.ceil(this.x);
        this.y = Math.ceil(this.y);
        this.z = Math.ceil(this.z);
        this.w = Math.ceil(this.w);
        return this;
    }

    public Vec4d cpy() {
        return new Vec4d(this.x, this.y, this.z, this.w);
    }

    public Vec4d d() {
        return new Vec4d(this.x, this.y, this.z, this.w);
    }

    public Vec4f f() {
        return new Vec4f((float) this.x, (float) this.y, (float) this.z, (float) this.w);
    }

    public Vec4i i() {
        return new Vec4i((int) this.x, (int) this.y, (int) this.z, (int) this.w);
    }

    @Override
    public Vec4d clone() {
        try {
            Vec4d clone = (Vec4d) super.clone();

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
        Vec4d vector4i = (Vec4d) o;
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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.z);
        out.writeDouble(this.w);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
        this.w = in.readDouble();
    }
}
