package dev.ultreon.quantum.util;

import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.CubicDirection;

import java.util.Objects;

public class BlockHitResult implements HitResult {
    protected CubicDirection direction;
    // input
    protected Ray ray;
    protected float distanceMax = 5.0F;
    protected Vec3d position = new Vec3d();
    protected Vec3d normal = new Vec3d();
    protected Vec3i pos = new Vec3i();
    protected Vec3i next = new Vec3i();
    protected BlockProperties blockMeta = BlockProperties.AIR;
    protected Block block = Blocks.AIR;
    protected boolean collide;
    protected double distance;

    public BlockHitResult() {

    }

    public BlockHitResult(Ray ray) {
        this.ray = ray;
        this.setDirection(ray.getDirection());
    }

    public BlockHitResult(Ray ray, float distanceMax) {
        this.ray = ray;
        this.setDirection(ray.getDirection());
        this.distanceMax = distanceMax;
        this.distance = distanceMax;
    }

    public BlockHitResult(PacketIO buffer) {
        this.ray = new Ray(buffer);
        this.setDirection(ray.getDirection());
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

    public BlockHitResult(Ray ray, BlockPos blockPos, BlockProperties block) {
        this.ray = ray;
        this.blockMeta = block;
        this.block = block.getBlock();
        this.position.set(blockPos.vec().d());
        this.setDirection(ray.getDirection());
        this.distanceMax = 5.0F;
        this.pos.set(blockPos.vec());
        this.next.set(blockPos.vec());
        this.normal.set(0, 0, 0);
        this.collide = true;
        this.distance = 0.0D;
    }

    public void write(PacketIO buffer) {
        this.ray.write(buffer);
        buffer.writeFloat(this.distanceMax);
        buffer.writeVec3d(this.position);
        buffer.writeVec3d(this.normal);
        buffer.writeVec3i(this.pos);
        buffer.writeVec3i(this.next);
        buffer.writeBlockMeta(this.getBlockMeta());
        buffer.writeVarInt(Registries.BLOCK.getRawId(this.getBlock()));
        buffer.writeBoolean(this.collide);
        buffer.writeDouble(this.getDistance());
    }

    public BlockHitResult setInput(Ray ray) {
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

    public Vec3i getNext() {
        return this.next;
    }

    public BlockProperties getBlockMeta() {
        return this.blockMeta;
    }

    public Block getBlock() {
        return this.block;
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
        BlockHitResult hitResult = (BlockHitResult) o;
        return Float.compare(distanceMax, hitResult.distanceMax) == 0 && collide == hitResult.collide && Double.compare(getDistance(), hitResult.getDistance()) == 0 && getDirection() == hitResult.getDirection() && Objects.equals(ray, hitResult.ray) && Objects.equals(position, hitResult.position) && Objects.equals(normal, hitResult.normal) && Objects.equals(pos, hitResult.pos) && Objects.equals(next, hitResult.next) && Objects.equals(getBlockMeta(), hitResult.getBlockMeta()) && Objects.equals(getBlock(), hitResult.getBlock());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDirection(), ray, distanceMax, position, normal, pos, next, getBlockMeta(), getBlock(), collide, getDistance());
    }

    public CubicDirection getDirection() {
        return direction;
    }

    public void setDirection(CubicDirection direction) {
        this.direction = direction;
    }
}