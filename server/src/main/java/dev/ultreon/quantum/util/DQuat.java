package dev.ultreon.quantum.util;

public class DQuat {
    public double x;
    public double y;
    public double z;
    public double w;
    
    public DQuat(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
    public DQuat() {
        this(0, 0, 0, 1);
    }
    
    public DQuat(DQuat other) {
        this(other.x, other.y, other.z, other.w);
    }
    
    public DQuat set(DQuat other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.w = other.w;
        return this;
    }
    
    public DQuat set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }
    
    public DQuat setIdentity() {
        return set(0, 0, 0, 1);
    }
    
    public DQuat setZero() {
        return set(0, 0, 0, 0);
    }
    
    public DQuat conjugate() {
        return set(-x, -y, -z, w);
    }
    
    public DQuat inverse() {
        return set(x, y, z, -w);
    }
}
