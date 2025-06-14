package dev.ultreon.quantum.util;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.vec.BlockVec;

import java.util.Objects;

public class BlockHit implements Hit {
    public static final BlockHit MISS = new BlockHit();
    protected Direction direction;
    // input
    protected Ray ray;
    protected float distanceMax = 5.0F;
    protected Vec position = new Vec();
    protected Vec normal = new Vec();
    protected BlockVec vec = new BlockVec();
    protected BlockState blockMeta = Blocks.AIR.getDefaultState();
    protected Block block = Blocks.AIR;
    protected boolean collide;
    protected float distance;

    public BlockHit() {

    }

    public BlockHit(Ray ray) {
        this.ray = ray;
        this.setDirection(ray.getDirection());
    }

    public BlockHit(Ray ray, float distanceMax) {
        this.ray = ray;
        this.setDirection(ray.getDirection());
        this.distanceMax = distanceMax;
        this.distance = distanceMax;
    }

    public BlockHit(PacketIO buffer) {
        this.ray = new Ray(buffer);
        this.setDirection(ray.getDirection());
        this.distanceMax = buffer.readFloat();
        this.position.set(buffer.readVec3d());
        this.normal.set(buffer.readVec3d());
        this.vec.set(buffer.readVec3i());
        this.blockMeta = buffer.readBlockState();
        this.block = Registries.BLOCK.byRawId(buffer.readVarInt());
        this.collide = buffer.readBoolean();
        this.distance = buffer.readFloat();
    }

    public BlockHit(Ray ray, BlockVec blockVec, BlockState block) {
        this.ray = ray;
        this.blockMeta = block;
        this.block = block.getBlock();
        this.position.set(blockVec.vec().d());
        this.setDirection(ray.getDirection());
        this.distanceMax = 5.0F;
        this.vec.set(blockVec.vec());
        this.normal.set(0, 0, 0);
        this.collide = true;
        this.distance = 0.0F;
    }

    public void write(PacketIO buffer) {
        this.ray.write(buffer);
        buffer.writeFloat(this.distanceMax);
        buffer.writeVec3d(this.position);
        buffer.writeVec3d(this.normal);
        buffer.writeVec3i(this.vec);
        buffer.writeBlockState(this.getBlockMeta());
        buffer.writeVarInt(Registries.BLOCK.getRawId(this.getBlock()));
        buffer.writeBoolean(this.collide);
        buffer.writeFloat(this.getDistance());
    }

    public BlockHit setInput(Ray ray) {
        this.ray = ray;
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
        return this.vec;
    }

    public BlockVec getNext() {
        return this.vec.cpy().add((int) this.normal.x, (int) this.normal.y, (int) this.normal.z);
    }

    public BlockState getBlockMeta() {
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
    public float getDistance() {
        return this.distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockHit hitResult = (BlockHit) o;
        return Float.compare(distanceMax, hitResult.distanceMax) == 0 && collide == hitResult.collide && Double.compare(getDistance(), hitResult.getDistance()) == 0 && getDirection() == hitResult.getDirection() && Objects.equals(ray, hitResult.ray) && Objects.equals(position, hitResult.position) && Objects.equals(normal, hitResult.normal) && Objects.equals(vec, hitResult.vec) && Objects.equals(getBlockMeta(), hitResult.getBlockMeta()) && Objects.equals(getBlock(), hitResult.getBlock());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDirection(), ray, distanceMax, position, normal, vec, getBlockMeta(), getBlock(), collide, getDistance());
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}