package dev.ultreon.quantum.util;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;

import java.util.Objects;

public class EntityHit implements Hit {
    public static final EntityHit MISS = new EntityHit();
    protected Direction direction;
    // input
    protected Ray ray;
    protected float distanceMax = 5.0F;
    public Vec position = new Vec();
    protected Vec normal = new Vec();
    protected BlockVec pos = new BlockVec(BlockVecSpace.WORLD);
    public Entity entity;
    public boolean collide;
    public float distance;

    public EntityHit() {

    }

    public EntityHit(Ray ray) {
        this.ray = ray;
        this.setDirection(ray.getDirection());
    }

    public EntityHit(Ray ray, float distanceMax) {
        this.ray = ray;
        this.setDirection(ray.getDirection());
        this.distanceMax = distanceMax;
        this.distance = distanceMax;
    }

    public EntityHit(PacketIO buffer) {
        this.ray = new Ray(buffer);
        this.setDirection(ray.getDirection());
        this.distanceMax = buffer.readFloat();
        this.position.set(buffer.readVec3d());
        this.normal.set(buffer.readVec3d());
        this.pos.set(buffer.readVec3i());
        this.collide = buffer.readBoolean();
        this.distance = buffer.readFloat();
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
        buffer.writeFloat(this.getDistance());
    }

    public EntityHit setInput(Ray ray) {
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
    public Vec getVec() {
        return this.position;
    }

    public Vec getNormal() {
        return this.normal;
    }

    @Override
    public BlockVec getBlockVec() {
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
    public float getDistance() {
        return this.distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityHit hitResult = (EntityHit) o;
        return Float.compare(distanceMax, hitResult.distanceMax) == 0 && collide == hitResult.collide && Double.compare(getDistance(), hitResult.getDistance()) == 0 && getDirection() == hitResult.getDirection() && Objects.equals(ray, hitResult.ray) && Objects.equals(position, hitResult.position) && Objects.equals(normal, hitResult.normal) && Objects.equals(pos, hitResult.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDirection(), ray, distanceMax, position, normal, pos, collide, getDistance());
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}