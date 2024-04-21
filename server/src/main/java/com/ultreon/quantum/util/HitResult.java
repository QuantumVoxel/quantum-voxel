package com.ultreon.quantum.util;

import com.ultreon.quantum.block.Block;
import com.ultreon.quantum.block.Blocks;
import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.world.CubicDirection;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.Objects;

public class HitResult {
    public CubicDirection direction;
    // input
    protected Ray ray;
    protected float distanceMax = 5.0F;
    protected Vec3d position = new Vec3d();
    protected Vec3d normal = new Vec3d();
    protected Vec3i pos = new Vec3i();
    protected Vec3i next = new Vec3i();
    public BlockProperties blockMeta = BlockProperties.AIR;
    public Block block = Blocks.AIR;
    public boolean collide;
    public double distance;

    public HitResult() {

    }

    public HitResult(Ray ray) {
        this.ray = ray;
        this.direction = ray.getDirection();
    }

    public HitResult(Ray ray, float distanceMax) {
        this.ray = ray;
        this.direction = ray.getDirection();
        this.distanceMax = distanceMax;
    }

    public HitResult(PacketIO buffer) {
        this.ray = new Ray(buffer);
        this.direction = ray.getDirection();
        this.distanceMax = buffer.readFloat();
        this.position.set(buffer.readVec3d());
        this.normal.set(buffer.readVec3d());
        this.pos.set(buffer.readVec3i());
        this.next.set(buffer.readVec3i());
        this.blockMeta = buffer.readBlockMeta();
        this.block = Registries.BLOCK.byId(buffer.readVarInt());
        this.collide = buffer.readBoolean();
        this.distance = buffer.readDouble();
    }

    public void write(PacketIO buffer) {
        this.ray.write(buffer);
        buffer.writeFloat(this.distanceMax);
        buffer.writeVec3d(this.position);
        buffer.writeVec3d(this.normal);
        buffer.writeVec3i(this.pos);
        buffer.writeVec3i(this.next);
        buffer.writeBlockMeta(this.blockMeta);
        buffer.writeVarInt(Registries.BLOCK.getRawId(this.block));
        buffer.writeBoolean(this.collide);
        buffer.writeDouble(this.distance);
    }

    public HitResult setInput(Ray ray) {
        this.ray = ray;
        this.direction = ray.getDirection();
        return this;
    }

    public Ray getRay() {
        return this.ray;
    }

    public float getDistanceMax() {
        return this.distanceMax;
    }

    public Vec3d getPosition() {
        return this.position;
    }

    public Vec3d getNormal() {
        return this.normal;
    }

    public Vec3i getPos() {
        return this.pos;
    }

    public Vec3i getNext() {
        return this.next;
    }

    public BlockProperties getBlockMeta() {
        return this.blockMeta;
    }

    public Block getBlock() {
        return this.block;
    }

    public boolean isCollide() {
        return this.collide;
    }

    public double getDistance() {
        return this.distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HitResult hitResult = (HitResult) o;
        return Float.compare(distanceMax, hitResult.distanceMax) == 0 && collide == hitResult.collide && Double.compare(distance, hitResult.distance) == 0 && direction == hitResult.direction && Objects.equals(ray, hitResult.ray) && Objects.equals(position, hitResult.position) && Objects.equals(normal, hitResult.normal) && Objects.equals(pos, hitResult.pos) && Objects.equals(next, hitResult.next) && Objects.equals(blockMeta, hitResult.blockMeta) && Objects.equals(block, hitResult.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(direction, ray, distanceMax, position, normal, pos, next, blockMeta, block, collide, distance);
    }
}