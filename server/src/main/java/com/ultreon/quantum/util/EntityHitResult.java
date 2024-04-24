package com.ultreon.quantum.util;

import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.ultreon.quantum.entity.Entity;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.world.CubicDirection;

import java.util.Objects;

public class EntityHitResult implements HitResult {
    protected CubicDirection direction;
    // input
    protected Ray ray;
    protected float distanceMax = 5.0F;
    public Vec3d position = new Vec3d();
    protected Vec3d normal = new Vec3d();
    protected Vec3i pos = new Vec3i();
    public Entity entity;
    public boolean collide;
    public double distance;

    public EntityHitResult() {

    }

    public EntityHitResult(Ray ray) {
        this.ray = ray;
        this.setDirection(ray.getDirection());
    }

    public EntityHitResult(Ray ray, float distanceMax) {
        this.ray = ray;
        this.setDirection(ray.getDirection());
        this.distanceMax = distanceMax;
        this.distance = distanceMax;
    }

    public EntityHitResult(PacketIO buffer) {
        this.ray = new Ray(buffer);
        this.setDirection(ray.getDirection());
        this.distanceMax = buffer.readFloat();
        this.position.set(buffer.readVec3d());
        this.normal.set(buffer.readVec3d());
        this.pos.set(buffer.readVec3i());
        this.collide = buffer.readBoolean();
        this.distance = buffer.readDouble();
    }

    public void write(PacketIO buffer) {
        this.ray.write(buffer);
        buffer.writeFloat(this.distanceMax);
        buffer.writeVec3d(this.position);
        buffer.writeVec3d(this.normal);
        buffer.writeVec3i(this.pos);
        buffer.writeUuid(this.entity.getWorld().getUID());
        buffer.writeInt(this.entity.getId());
        buffer.writeBoolean(this.collide);
        buffer.writeDouble(this.getDistance());
    }

    public EntityHitResult setInput(Ray ray) {
        this.ray = ray;
        this.setDirection(ray.getDirection());
        return this;
    }

    @Override
    public Ray getRay() {
        return this.ray;
    }

    @Override
    public float getDistanceMax() {
        return this.distanceMax;
    }

    @Override
    public Vec3d getPosition() {
        return this.position;
    }

    public Vec3d getNormal() {
        return this.normal;
    }

    @Override
    public Vec3i getPos() {
        return this.pos;
    }

    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public boolean isCollide() {
        return this.collide;
    }

    @Override
    public double getDistance() {
        return this.distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityHitResult hitResult = (EntityHitResult) o;
        return Float.compare(distanceMax, hitResult.distanceMax) == 0 && collide == hitResult.collide && Double.compare(getDistance(), hitResult.getDistance()) == 0 && getDirection() == hitResult.getDirection() && Objects.equals(ray, hitResult.ray) && Objects.equals(position, hitResult.position) && Objects.equals(normal, hitResult.normal) && Objects.equals(pos, hitResult.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDirection(), ray, distanceMax, position, normal, pos, collide, getDistance());
    }

    public CubicDirection getDirection() {
        return direction;
    }

    public void setDirection(CubicDirection direction) {
        this.direction = direction;
    }
}