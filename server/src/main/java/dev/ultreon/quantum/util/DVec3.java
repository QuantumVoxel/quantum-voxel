package dev.ultreon.quantum.util;

import java.util.Objects;

/**
 * A 3D Integer vector is a vector that has three dimensions, each of which is a double.
 * This vector is used in many places in the game, such as representing the position of a block in the world.
 * It is also used for other data that has three dimensions, such as the size of a block model.
 *
 * @author XyperCode
 * @see DVec2
 * @see IVec3
 * @see Vec3
 */
@SuppressWarnings("unused")
public class DVec3 implements Cloneable {
    public double x;
    public double y;
    public double z;

    public DVec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public DVec3(DVec2 vec, double z) {
        this(vec.x, vec.y, z);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public DVec3(double x, DVec2 vec) {
        this(x, vec.x, vec.y);
    }

    public DVec3(IVec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public DVec3(Vec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public DVec3(DVec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public DVec3(Point p) {
        this(p.getX(), p.getY(), p.getZ());
    }

    public DVec3() {
        this(0.0, 0.0, 0.0);
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

    public static DVec3 scl(DVec3 a, DVec3 b) {
        return new DVec3(a.x * b.x, a.y * b.y, a.z * b.z);
    }

    public static DVec3 div(DVec3 a, DVec3 b) {
        return new DVec3(a.x / b.x, a.y / b.y, a.z / b.z);
    }

    public static DVec3 add(DVec3 a, DVec3 b) {
        return new DVec3(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static DVec3 sub(DVec3 a, DVec3 b) {
        return new DVec3(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static double dot(DVec3 a, DVec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static DVec3 pow(DVec3 a, DVec3 b) {
        return new DVec3(Math.pow(a.x, b.x), Math.pow(a.y, b.y), Math.pow(a.z, b.z));
    }

    public double dot(DVec3 vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public double dot(double x, double y, double z) {
        return this.x * x + this.y * y + this.z * z;
    }

    public double dot(double v) {
        return this.x * v + this.y * v + this.z * v;
    }

    public double len2 () {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public DVec3 nor () {
        final double len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return this;
        return this.scl(1f / (float)Math.sqrt(len2));
    }

    public double dst(DVec3 vec) {
        double a = vec.x - this.x;
        double b = vec.y - this.y;
        double c = vec.z - this.z;
        return Math.sqrt(a * a + b * b + c * c);
    }

    public double dst(double x, double y, double z) {
        double a = x - this.x;
        double b = y - this.y;
        double c = z - this.z;
        return Math.sqrt(a * a + b * b + c * c);
    }

    public DVec3 set(DVec3 vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        return this;
    }

    public DVec3 set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public DVec3 set(double v) {
        this.x = v;
        this.y = v;
        this.z = v;
        return this;
    }

    public DVec3 add(DVec3 vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;
    }

    public DVec3 add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public DVec3 add(double v) {
        this.x += v;
        this.y += v;
        this.z += v;
        return this;
    }

    public DVec3 sub(DVec3 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        return this;
    }

    public DVec3 sub(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public DVec3 sub(double v) {
        this.x -= v;
        this.y -= v;
        this.z -= v;
        return this;
    }

    public DVec3 scl(DVec3 vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;
        return this;
    }

    public DVec3 scl(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public DVec3 scl(double v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        return this;
    }

    public DVec3 div(DVec3 vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        this.z /= vec.z;
        return this;
    }

    public DVec3 div(double x, double y, double z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    public DVec3 div(double v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        return this;
    }

    public DVec3 mod(DVec3 vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        this.z %= vec.z;
        return this;
    }

    public DVec3 mod(double x, double y, double z) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        return this;
    }

    public DVec3 mod(double v) {
        this.x %= v;
        this.y %= v;
        this.z %= v;
        return this;
    }

    public DVec3 pow(DVec3 vec) {
        this.x = Math.pow(this.x, vec.x);
        this.y = Math.pow(this.y, vec.y);
        this.z = Math.pow(this.z, vec.z);
        return this;
    }

    public DVec3 pow(double x, double y, double z) {
        this.x = Math.pow(this.x, x);
        this.y = Math.pow(this.y, y);
        this.z = Math.pow(this.z, z);
        return this;
    }

    public DVec3 pow(double v) {
        this.x = Math.pow(this.x, v);
        this.y = Math.pow(this.y, v);
        this.z = Math.pow(this.z, v);
        return this;
    }

    public DVec3 neg() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    public DVec3 inc() {
        this.x++;
        this.y++;
        this.z++;
        return this;
    }

    public DVec3 dec() {
        this.x--;
        this.y--;
        this.z--;
        return this;
    }

    public DVec3 abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        return this;
    }

    public DVec3 floor() {
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        this.z = Math.floor(this.z);
        return this;
    }

    public DVec3 ceil() {
        this.x = Math.ceil(this.x);
        this.y = Math.ceil(this.y);
        this.z = Math.ceil(this.z);
        return this;
    }

    public DVec3 cpy() {
        return new DVec3(this.x, this.y, this.z);
    }

    public DVec3 d() {
        return new DVec3(this.x, this.y, this.z);
    }

    public Vec3 f() {
        return new Vec3((float) this.x, (float) this.y, (float) this.z);
    }

    public IVec3 i() {
        return new IVec3((int) this.x, (int) this.y, (int) this.z);
    }

    @Override
    public DVec3 clone() {
        try {
            DVec3 clone = (DVec3) super.clone();

            clone.x = this.x;
            clone.y = this.y;
            clone.z = this.z;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        DVec3 vector4i = (DVec3) o;
        return this.getX() == vector4i.getX() && this.getY() == vector4i.getY() && this.getZ() == vector4i.getZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getX(), this.getY(), this.getZ());
    }

    @Override
    public String toString() {
        return String.format("%f, %f, %f", this.x, this.y, this.z);
    }

    public double distanceSquared(DVec3 position) {
        double dx = this.x - position.x;
        double dy = this.y - position.y;
        double dz = this.z - position.z;
        return dx * dx + dy * dy + dz * dz;
    }
}
