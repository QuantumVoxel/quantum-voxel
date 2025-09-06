package dev.ultreon.quantum.util;

public class Quat {
    public float x;
    public float y;
    public float z;
    public float w;

    public Quat(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quat() {
        this(0, 0, 0, 1);
    }

    public Quat(Quat other) {
        this(other.x, other.y, other.z, other.w);
    }

    public Quat set(Quat other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.w = other.w;
        return this;
    }

    public Quat set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Quat setIdentity() {
        return set(0, 0, 0, 1);
    }

    public Quat setZero() {
        return set(0, 0, 0, 0);
    }

    public Quat conjugate() {
        return set(-x, -y, -z, w);
    }

    public Quat inverse() {
        return set(x, y, z, -w);
    }
}
