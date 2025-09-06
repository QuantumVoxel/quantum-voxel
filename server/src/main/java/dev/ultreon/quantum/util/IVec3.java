package dev.ultreon.quantum.util;

import java.util.Objects;

/**
 * A 3D Integer vector is a vector that has three dimensions, each of which is an integer.
 * This vector is used in many places in the game, such as representing the position of a block in the world.
 * It is also used for other data that has three dimensions, such as the size of a block model.
 *
 * @author XyperCode
 * @see IVec2
 * @see Vec3
 * @see DVec3
 */
@SuppressWarnings("unused")
public class IVec3 implements Cloneable {
    public int x, y, z;

    /**
     * Creates a new {@link IVec3} with the given x, y, and z.
     * @param x the x position
     * @param y the y position
     * @param z the z position
     */
    public IVec3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a new {@link IVec3} with the given {@link IVec2} (as x, y) and z.
     * @param vec the Vec2i
     * @param z the z position
     */
    public IVec3(IVec2 vec, int z) {
        this(vec.x, vec.y, z);
    }

    /**
     * Creates a new {@link IVec3} with the given x and {@link IVec2} (as y, z).
     * @param x the x position
     * @param vec the {@link IVec2}
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public IVec3(int x, IVec2 vec) {
        this(x, vec.x, vec.y);
    }

    /**
     * Creates a new {@link IVec3} with the given {@link IVec3}.
     * @param vec the Vec3i
     */
    public IVec3(IVec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    /**
     * Creates a new {@link IVec3} with the given {@link Point}.
     * @param vec the Point
     */
    public IVec3(Point vec) {
        this(vec.getIntX(), vec.getIntY(), vec.getIntZ());
    }

    /**
     * Creates a new Vec3i with the default values.
     */
    public IVec3() {

    }
    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = (int) x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = (int) y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = (int) z;
    }

    public int getIntX() {
        return this.x;
    }

    public void setBlockX(int x) {
        this.x = x;
    }

    public int getIntY() {
        return this.y;
    }

    public void setBlockY(int y) {
        this.y = y;
    }

    public int getIntZ() {
        return this.z;
    }

    public void setBlockZ(int z) {
        this.z = z;
    }

    public static IVec3 mul(IVec3 a, IVec3 b) {
        return new IVec3(a.x * b.x, a.y * b.y, a.z * b.z);
    }

    public static IVec3 div(IVec3 a, IVec3 b) {
        return new IVec3(a.x / b.x, a.y / b.y, a.z / b.z);
    }

    public static IVec3 add(IVec3 a, IVec3 b) {
        return new IVec3(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static IVec3 sub(IVec3 a, IVec3 b) {
        return new IVec3(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static int dot(IVec3 a, IVec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static DVec3 pow(IVec3 a, IVec3 b) {
        return new DVec3(Math.pow(a.x, b.x), Math.pow(a.y, b.y), Math.pow(a.z, b.z));
    }

    public int dot(IVec3 vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public int dot(int x, int y, int z) {
        return this.x * x + this.y * y + this.z * z;
    }

    public int dot(int v) {
        return this.x * v + this.y * v + this.z * v;
    }

    public double dst(IVec3 vec) {
        int a = vec.x - this.x;
        int b = vec.y - this.y;
        int c = vec.z - this.z;
        return Math.sqrt(a * a + b * b + c * c);
    }

    public double dst(int x, int y, int z) {
        int a = x - this.x;
        int b = y - this.y;
        int c = z - this.z;
        return Math.sqrt(a * a + b * b + c * c);
    }

    public IVec3 set(IVec3 vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        return this;
    }

    public IVec3 set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public IVec3 set(int v) {
        this.x = v;
        this.y = v;
        this.z = v;
        return this;
    }

    public IVec3 add(IVec3 vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;
    }

    public IVec3 add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public IVec3 add(int v) {
        this.x += v;
        this.y += v;
        this.z += v;
        return this;
    }

    public IVec3 sub(IVec3 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        return this;
    }

    public IVec3 sub(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public IVec3 sub(int v) {
        this.x -= v;
        this.y -= v;
        this.z -= v;
        return this;
    }

    public IVec3 mul(IVec3 vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;
        return this;
    }

    public IVec3 mul(int x, int y, int z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public IVec3 mul(int v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        return this;
    }

    public IVec3 div(IVec3 vec) {
        this.x /= vec.x;
        this.y /= vec.y;
        this.z /= vec.z;
        return this;
    }

    public IVec3 div(int x, int y, int z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    public IVec3 div(int v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        return this;
    }

    public IVec3 mod(IVec3 vec) {
        this.x %= vec.x;
        this.y %= vec.y;
        this.z %= vec.z;
        return this;
    }

    public IVec3 mod(int x, int y, int z) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        return this;
    }

    public IVec3 mod(int v) {
        this.x %= v;
        this.y %= v;
        this.z %= v;
        return this;
    }

    public IVec3 pow(IVec3 vec) {
        this.x = (int) Math.pow(this.x, vec.x);
        this.y = (int) Math.pow(this.y, vec.y);
        this.z = (int) Math.pow(this.z, vec.z);
        return this;
    }

    public IVec3 pow(int x, int y, int z) {
        this.x = (int) Math.pow(this.x, x);
        this.y = (int) Math.pow(this.y, y);
        this.z = (int) Math.pow(this.z, z);
        return this;
    }

    public IVec3 pow(int v) {
        this.x = (int) Math.pow(this.x, v);
        this.y = (int) Math.pow(this.y, v);
        this.z = (int) Math.pow(this.z, v);
        return this;
    }

    public IVec3 neg() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    public IVec3 inc() {
        this.x++;
        this.y++;
        this.z++;
        return this;
    }

    public IVec3 dec() {
        this.x--;
        this.y--;
        this.z--;
        return this;
    }

    public IVec3 abs() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        return this;
    }

    public IVec3 cpy() {
        return new IVec3(this.x, this.y, this.z);
    }

    public DVec3 d() {
        return new DVec3(this.x, this.y, this.z);
    }

    public Vec3 f() {
        return new Vec3(this.x, this.y, this.z);
    }

    public IVec3 i() {
        return new IVec3(this.x, this.y, this.z);
    }

    @Override
    public IVec3 clone() {
        try {
            IVec3 clone = (IVec3) super.clone();

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
        IVec3 vector4i = (IVec3) o;
        return this.getIntX() == vector4i.getIntX() && this.getIntY() == vector4i.getIntY() && this.getIntZ() == vector4i.getIntZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getIntX(), this.getIntY(), this.getIntZ());
    }

    @Override
    public String toString() {
        return String.format("%d, %d, %d", this.x, this.y, this.z);
    }

    public long seed() {
        long result = x;
        result = 31L * result + y;
        result = 31L * result + z;
        java.util.Random current = new java.util.Random(result);
        return current.nextLong();
    }
}
