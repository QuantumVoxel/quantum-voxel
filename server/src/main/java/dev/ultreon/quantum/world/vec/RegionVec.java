package dev.ultreon.quantum.world.vec;

import dev.ultreon.quantum.util.Point;
import dev.ultreon.quantum.util.Vec3i;

import java.util.Objects;

import static dev.ultreon.quantum.world.World.CS;
import static dev.ultreon.quantum.world.World.REGION_SIZE;

/**
 * Represents a region position in the world.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public final class RegionVec extends Vec3i {
    /**
     * Creates a new region position with the specified coordinates.
     *
     * @param x the x-coordinate of the region position
     * @param y the y-coordinate of the region position
     * @param z the z-coordinate of the region position
     */
    public RegionVec(int x, int y, int z) {
        super(x, y, z);
    }

    /**
     * Creates a new region position with the specified coordinates.
     *
     * @param point the coordinates of the region position
     */
    public RegionVec(Point point) {
        super(point.getIntX(), point.getIntY(), point.getIntZ());
    }

    /**
     * Creates a new region position with the specified coordinates.
     */
    public RegionVec() {
        super();
    }

    public BlockVec blockInWorld(int x, int y, int z) {
        return new BlockVec(this.x * REGION_SIZE * CS + x, this.y * REGION_SIZE * CS + y, this.z * REGION_SIZE * CS + z);
    }

    public BlockVec blockInChunk(int x, int y, int z) {
        int cx = x % CS;
        int cy = y % CS;
        int cz = z % CS;

        if (cx < 0) cx += CS;
        if (cy < 0) cy += CS;
        if (cz < 0) cz += CS;

        return new BlockVec(this.x * REGION_SIZE * CS + cx, this.y * REGION_SIZE * CS + cy, this.z * REGION_SIZE * CS + cz);
    }

    public BlockVec blockInChunk(Vec3i vec) {
        return this.blockInChunk(vec.x, vec.y, vec.z);
    }

    public ChunkVec chunkInWorld(int x, int y, int z) {
        return new ChunkVec(this.x * REGION_SIZE + x, this.y * REGION_SIZE + y, this.z * REGION_SIZE + z, ChunkVecSpace.WORLD);
    }

    public ChunkVec chunkInWorld(Vec3i vec) {
        return new ChunkVec(this.x * REGION_SIZE + vec.x, this.y * REGION_SIZE + vec.y, this.z * REGION_SIZE + vec.z, ChunkVecSpace.WORLD);
    }

    public ChunkVec chunkInRegion(int x, int y, int z) {
        int rx = x % REGION_SIZE;
        int ry = y % REGION_SIZE;
        int rz = z % REGION_SIZE;

        if (rx < 0) rx += REGION_SIZE;
        if (ry < 0) ry += REGION_SIZE;
        if (rz < 0) rz += REGION_SIZE;

        return new ChunkVec(rx, ry, rz, ChunkVecSpace.REGION);
    }

    public ChunkVec chunkInRegion(Vec3i vec) {
        return chunkInRegion(vec.x, vec.y, vec.z);
    }

    public RegionVec offset(Vec3i vec) {
        return new RegionVec(this.x + vec.x, this.y + vec.y, this.z + vec.z);
    }

    public RegionVec offset(int x, int y, int z) {
        return new RegionVec(this.x + x, this.y + y, this.z + z);
    }

    public RegionVec offset(Point point) {
        return new RegionVec(this.x + point.getIntX(), this.y + point.getIntY(), this.z + point.getIntZ());
    }

    @Override
    public String toString() {
        return this.x + "," + this.y + "," + this.z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RegionVec) obj;
        return this.x == that.x &&
               this.y == that.y &&
               this.z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

}
