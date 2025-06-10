package dev.ultreon.quantum.world.vec;

import dev.ultreon.quantum.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

import static dev.ultreon.quantum.world.World.CS;
import static dev.ultreon.quantum.world.World.REGION_SIZE;

/**
 * Represents the position of a chunk in the world.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public final class ChunkVec extends Vec3i implements Comparable<ChunkVec>, Serializable {
    // Region chunk size
    public static final int RCS = REGION_SIZE * CS;

    private final ChunkVecSpace space;

    /**
     * @param x The x coordinate.
     * @param z The z coordinate.
     */
    @Deprecated
    public ChunkVec(int x, int y, int z) {
        this(x, y, z, ChunkVecSpace.WORLD);
    }

    @Deprecated
    public ChunkVec() {
        this(ChunkVecSpace.WORLD);
    }

    public ChunkVec(int x, int y, int z, ChunkVecSpace space) {
        super(x, y, z);

        space.validate(x, y, z);
        this.space = space;
    }

    public ChunkVec(ChunkVecSpace space) {
        this(0, 0, 0, space);
    }

    /**
     * Converts this chunk position to a string.
     *
     * @return The string representation of this chunk position.
     */
    @Override
    public String toString() {
        return this.x + "," + this.y + "," + this.z;
    }

    /**
     * Parse a string into a chunk position.
     *
     * @param s The string to parse.
     * @return The parsed chunk position, or {@code null} if the string cannot be parsed.
     */
    @Nullable
    public static ChunkVec parse(String s) {
        String[] split = s.split(",", 3);
        if (split.length == 2) {
            Integer x = ChunkVec.parseInt(split[0]);
            Integer z = ChunkVec.parseInt(split[1]);
            if (x == null) return null;
            if (z == null) return null;
            return new ChunkVec(x, 0, z);
        } else if (split.length == 3) {
            Integer x = ChunkVec.parseInt(split[0]);
            Integer y = ChunkVec.parseInt(split[1]);
            Integer z = ChunkVec.parseInt(split[2]);
            if (x == null) return null;
            if (y == null) return null;
            if (z == null) return null;
            return new ChunkVec(x, y, z);
        }
        return null;
    }

    @Nullable
    private static Integer parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * @return The origin of the chunk.
     */
    public Vec3d getChunkOrigin() {
        return new Vec3d(this.x * CS, this.y * CS, this.z * CS);
    }

    /**
     * Compare this chunk position to another.
     *
     * @param chunkVec the chunk position to be compared.
     * @return the comparison result.
     */
    @Override
    public int compareTo(@NotNull ChunkVec chunkVec) {
        double dst = this.dst(chunkVec);
        return dst == 0 ? 0 : dst < 0 ? -1 : 1;
    }

    @Deprecated
    public Vec2d vec() {
        return new Vec2d(this.x, this.z);
    }

    public Vec3d vec3d() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public ChunkVec offset(int x, int y, int z) {
        return new ChunkVec(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Returns a new ChunkVec in region space.
     *
     * @return A new ChunkVec in region space.
     */
    public ChunkVec regionLocal() {
        if (this.space == ChunkVecSpace.REGION) return this;

        int rx = this.x % REGION_SIZE;
        int ry = this.y % REGION_SIZE;
        int rz = this.z % REGION_SIZE;

        if (rx < 0) rx += REGION_SIZE;
        if (ry < 0) ry += REGION_SIZE;
        if (rz < 0) rz += REGION_SIZE;

        return new ChunkVec(rx, ry, rz, ChunkVecSpace.REGION);
    }

    /**
     * Converts this ChunkVec to the specified ChunkVecSpace, if necessary.
     *
     * @param space The ChunkVecSpace to convert to.
     * @return A new ChunkVec in the specified ChunkVecSpace, or this ChunkVec if it is already in the specified space.
     */
    public ChunkVec localize(ChunkVecSpace space) {
        if (this.space == space || !(this.space == ChunkVecSpace.WORLD && space == ChunkVecSpace.REGION)) return this;
        else {
            // Convert world space to region space
            int rx = this.x % REGION_SIZE;
            int ry = this.y % REGION_SIZE;
            int rz = this.z % REGION_SIZE;

            if (rx < 0) rx += REGION_SIZE;
            if (ry < 0) ry += REGION_SIZE;
            if (rz < 0) rz += REGION_SIZE;

            return new ChunkVec(rx, ry, rz, space);
        }
    }

    /**
     * Checks if this {@link ChunkVec} is equal to the given object.
     *
     * @param o The object to compare to.
     * @return {@code true} if this {@link ChunkVec} is equal to the given object, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkVec chunkVec = (ChunkVec) o;
        return x == chunkVec.x && y == chunkVec.y && z == chunkVec.z;
    }

    /**
     * Calculates the hash code of this {@link ChunkVec}.
     *
     * @return The hash code of this {@link ChunkVec}.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    /**
     * Returns the {@link RegionVec} of the region containing this {@link ChunkVec}.
     *
     * @return the {@link RegionVec} of the region containing this {@link ChunkVec}.
     */
    public RegionVec region() {
        int rx = this.x / REGION_SIZE;
        int ry = this.y / REGION_SIZE;
        int rz = this.z / REGION_SIZE;

        if (this.x < 0 && this.x % REGION_SIZE != 0) rx--;
        if (this.y < 0 && this.y % REGION_SIZE != 0) ry--;
        if (this.z < 0 && this.z % REGION_SIZE != 0) rz--;

        return new RegionVec(rx, ry, rz);
    }

    /**
     * Returns the position of the first block in this chunk.
     *
     * @return the position of the first block in this chunk.
     */
    public BlockVec start() {
        int cx = this.x * CS;
        int cy = this.y * CS;
        int cz = this.z * CS;

        return new BlockVec(cx, cy, cz, this.space.block());
    }

    /**
     * Returns the position of the last block in this chunk.
     *
     * @return the position of the last block in this chunk.
     */
    public BlockVec end() {
        int cx = this.x * CS;
        int cy = this.y * CS;
        int cz = this.z * CS;

        return new BlockVec(cx + CS - 1, cy + CS - 1, cz + CS - 1, this.space.block());
    }

    /**
     * Converts a block position in chunk space to world space.
     *
     * @param x      The x position in chunk space.
     * @param y      The y position in chunk space.
     * @param z      The z position in chunk space.
     * @param region The region to convert from, or null if converting from world space.
     * @return The position in world space.
     * @throws IllegalArgumentException if the region is null and the current space is in region space
     */
    public BlockVec blockInWorldSpace(int x, int y, int z, @Nullable RegionVec region) {
        var start = start();
        return new BlockVec(start.x + x, start.y + y, start.z + z, this.space.block());
    }

    /**
     * Converts a block position in chunk space to world space.
     *
     * @param x The x position in chunk space.
     * @param y The y position in chunk space.
     * @param z The z position in chunk space.
     * @return The position in world space.
     * @throws IllegalArgumentException if the region is null and the current space is in region space
     */
    public BlockVec blockInWorldSpace(int x, int y, int z) {
        return blockInWorldSpace(x, y, z, null);
    }

    /**
     * Converts a block position in chunk space to world space.
     *
     * @param block  The block position in chunk space.
     * @param region The region to convert from, or null if converting from world space.
     * @return The position in world space.
     * @throws IllegalArgumentException if the region is null and the current space is in region space
     */
    public BlockVec blockInWorldSpace(BlockVec block, @Nullable RegionVec region) {
        return blockInWorldSpace(block.x, block.y, block.z, region);
    }

    /**
     * Converts a block position in chunk space to world space.
     *
     * @param block The block position in chunk space.
     * @return The position in world space.
     * @throws IllegalArgumentException if the region is null and the current space is in region space
     */
    public BlockVec blockInWorldSpace(BlockVec block) {
        return blockInWorldSpace(block.x, block.y, block.z, null);
    }

    public BlockVec blockAt(int x, int y, int z) {
        int cx = this.x * CS + x;
        int cy = this.y * CS + y;
        int cz = this.z * CS + z;

        if (this.x < 0) cx += CS;
        if (this.y < 0) cy += CS;
        if (this.z < 0) cz += CS;

        return new BlockVec(cx, cy, cz, this.space.block());
    }

    /**
     * Converts this chunk position into region local space.
     *
     * @return The chunk position in region space.
     */
    public ChunkVec regionSpace() {
        if (this.x >= 0 && this.z >= 0 && this.y >= 0 && this.y < REGION_SIZE && this.x < REGION_SIZE && this.z < REGION_SIZE) {
            return new ChunkVec(this.x, this.y, this.z, ChunkVecSpace.REGION);
        }

        int rx = this.x < 0
                ? (this.x * REGION_SIZE + x - REGION_SIZE) % REGION_SIZE + REGION_SIZE
                : (this.x * REGION_SIZE + x) % REGION_SIZE;

        int ry = this.y < 0
                ? (this.y * REGION_SIZE + y - REGION_SIZE) % REGION_SIZE + REGION_SIZE
                : (this.y * REGION_SIZE + y) % REGION_SIZE;

        int rz = this.z < 0
                ? (this.z * REGION_SIZE + z - REGION_SIZE) % REGION_SIZE + REGION_SIZE
                : (this.z * REGION_SIZE + z) % REGION_SIZE;

        return new ChunkVec(rx, ry, rz, ChunkVecSpace.REGION);
    }

    /**
     * Converts this chunk position (in region space) to a world space.
     *
     * @param region The region position in world space.
     * @return The chunk position in world space.
     */
    public ChunkVec worldSpace(RegionVec region) {
        return new ChunkVec(region.x * REGION_SIZE + this.x, region.y * REGION_SIZE + this.y, region.z * REGION_SIZE + this.z, ChunkVecSpace.WORLD);
    }

    /**
     * Converts a block position to a specific space.
     * <p>
     * NOTE: This method assumes the current vector is in global space.
     *
     * @param x           the x position
     * @param y           the y position
     * @param z           the z position
     * @param targetSpace the space to convert to
     * @return the block position in the specified space
     */
    public BlockVec blockTo(int x, int y, int z, BlockVecSpace targetSpace) {
        if (this.space.block() == targetSpace) return new BlockVec(x, y, z, this.space.block());
        if (this.space.block() != BlockVecSpace.WORLD)
            throw new IllegalArgumentException("The target space is not in world space: " + targetSpace);

        BlockVec curSpaceBlock = new BlockVec(x, y, z, this.space.block());
        return curSpaceBlock.toSpace(targetSpace);
    }

    @Override
    public ChunkVec add(int x, int y, int z) {
        return new ChunkVec(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public ChunkVec add(Vec3i vec) {
        return new ChunkVec(this.x + vec.x, this.y + vec.y, this.z + vec.z);
    }

    public ChunkVec add(ChunkVec vec) {
        return new ChunkVec(this.x + vec.x, this.y + vec.y, this.z + vec.z);
    }

    @Override
    public ChunkVec sub(int x, int y, int z) {
        return new ChunkVec(this.x - x, this.y - y, this.z - z);
    }

    @Override
    public ChunkVec sub(Vec3i vec) {
        return new ChunkVec(this.x - vec.x, this.y - vec.y, this.z - vec.z);
    }

    public ChunkVec sub(ChunkVec vec) {
        return new ChunkVec(this.x - vec.x, this.y - vec.y, this.z - vec.z);
    }

    @Override
    public ChunkVec mul(int x, int y, int z) {
        return new ChunkVec(this.x * x, this.y * y, this.z * z);
    }

    @Override
    public ChunkVec mul(Vec3i vec) {
        return new ChunkVec(this.x * vec.x, this.y * vec.y, this.z * vec.z);
    }

    public ChunkVec mul(ChunkVec vec) {
        return new ChunkVec(this.x * vec.x, this.y * vec.y, this.z * vec.z);
    }

    @Override
    public ChunkVec div(int x, int y, int z) {
        return new ChunkVec(this.x / x, this.y / y, this.z / z);
    }

    @Override
    public ChunkVec div(Vec3i vec) {
        return new ChunkVec(this.x / vec.x, this.y / vec.y, this.z / vec.z);
    }

    public ChunkVec div(ChunkVec vec) {
        return new ChunkVec(this.x / vec.x, this.y / vec.y, this.z / vec.z);
    }

    @Override
    public ChunkVec mod(int x, int y, int z) {
        return new ChunkVec(this.x % x, this.y % y, this.z % z);
    }

    @Override
    public ChunkVec mod(Vec3i vec) {
        return new ChunkVec(this.x % vec.x, this.y % vec.y, this.z % vec.z);
    }

    public ChunkVec mod(ChunkVec vec) {
        return new ChunkVec(this.x % vec.x, this.y % vec.y, this.z % vec.z);
    }

    @Override
    public ChunkVec abs() {
        return new ChunkVec(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));
    }

    @Override
    public ChunkVec neg() {
        return new ChunkVec(-this.x, -this.y, -this.z);
    }

    @Override
    public ChunkVec clone() {
        return (ChunkVec) super.clone();
    }

    @Override
    public ChunkVec cpy() {
        return new ChunkVec(this.x, this.y, this.z);
    }

    @Override
    public ChunkVec add(int v) {
        return new ChunkVec(this.x + v, this.y + v, this.z + v);
    }

    @Override
    public ChunkVec sub(int v) {
        return new ChunkVec(this.x - v, this.y - v, this.z - v);
    }

    @Override
    public ChunkVec mul(int v) {
        return new ChunkVec(this.x * v, this.y * v, this.z * v);
    }

    @Override
    public ChunkVec div(int v) {
        return new ChunkVec(this.x / v, this.y / v, this.z / v);
    }

    @Override
    public ChunkVec mod(int v) {
        return new ChunkVec(this.x % v, this.y % v, this.z % v);
    }

    public int dot(ChunkVec vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public ChunkVec cross(ChunkVec vec) {
        return new ChunkVec(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z, this.x * vec.y - this.y * vec.x);
    }

    public ChunkVec max(ChunkVec vec) {
        return new ChunkVec(Math.max(this.x, vec.x), Math.max(this.y, vec.y), Math.max(this.z, vec.z));
    }

    public ChunkVec min(ChunkVec vec) {
        return new ChunkVec(Math.min(this.x, vec.x), Math.min(this.y, vec.y), Math.min(this.z, vec.z));
    }

    @Override
    public ChunkVec dec() {
        return new ChunkVec(this.x - 1, this.y - 1, this.z - 1);
    }

    @Override
    public ChunkVec inc() {
        return new ChunkVec(this.x + 1, this.y + 1, this.z + 1);
    }

    public ChunkVec pow(int v) {
        return new ChunkVec((int) Math.pow(this.x, v), (int) Math.pow(this.y, v), (int) Math.pow(this.z, v));
    }

    public ChunkVec sqrt() {
        return new ChunkVec((int) Math.sqrt(this.x), (int) Math.sqrt(this.y), (int) Math.sqrt(this.z));
    }

    @Override
    public ChunkVec pow(Vec3i vec) {
        return new ChunkVec((int) Math.pow(this.x, vec.x), (int) Math.pow(this.y, vec.y), (int) Math.pow(this.z, vec.z));
    }

    public ChunkVec pow(Vec3f vec) {
        return new ChunkVec((int) Math.pow(this.x, vec.x), (int) Math.pow(this.y, vec.y), (int) Math.pow(this.z, vec.z));
    }

    public ChunkVec pow(Vec3d vec) {
        return new ChunkVec((int) Math.pow(this.x, vec.x), (int) Math.pow(this.y, vec.y), (int) Math.pow(this.z, vec.z));
    }

    public ChunkVec max(Vec3i vec) {
        return new ChunkVec(Math.max(this.x, vec.x), Math.max(this.y, vec.y), Math.max(this.z, vec.z));
    }

    public ChunkVec min(Vec3i vec) {
        return new ChunkVec(Math.min(this.x, vec.x), Math.min(this.y, vec.y), Math.min(this.z, vec.z));
    }

    public ChunkVec pow(double x, double y, double z) {
        return new ChunkVec((int) Math.pow(this.x, x), (int) Math.pow(this.y, y), (int) Math.pow(this.z, z));
    }

    public ChunkVec max(int x, int y, int z) {
        return new ChunkVec(Math.max(this.x, x), Math.max(this.y, y), Math.max(this.z, z));
    }

    public ChunkVec min(int x, int y, int z) {
        return new ChunkVec(Math.min(this.x, x), Math.min(this.y, y), Math.min(this.z, z));
    }

    public ChunkVec pow(Point point) {
        return new ChunkVec((int) Math.pow(this.x, point.getX()), (int) Math.pow(this.y, point.getY()), (int) Math.pow(this.z, point.getZ()));
    }

    public ChunkVec max(Point point) {
        return new ChunkVec((int) Math.max(this.x, point.getX()), (int) Math.max(this.y, point.getY()), (int) Math.max(this.z, point.getZ()));
    }

    public ChunkVec min(Point point) {
        return new ChunkVec((int) Math.min(this.x, point.getX()), (int) Math.min(this.y, point.getY()), (int) Math.min(this.z, point.getZ()));
    }

    public ChunkVec pow(double v) {
        return new ChunkVec((int) Math.pow(this.x, v), (int) Math.pow(this.y, v), (int) Math.pow(this.z, v));
    }

    public ChunkVec max(int v) {
        return new ChunkVec(Math.max(this.x, v), Math.max(this.y, v), Math.max(this.z, v));
    }

    public ChunkVec min(int v) {
        return new ChunkVec(Math.min(this.x, v), Math.min(this.y, v), Math.min(this.z, v));
    }

    public ChunkVecSpace getSpace() {
        return space;
    }

    public boolean equals(int x, int y, int z) {
        return x == getIntX() && y == getIntY() && z == getIntZ();
    }
}
