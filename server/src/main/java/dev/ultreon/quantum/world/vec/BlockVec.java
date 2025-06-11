package dev.ultreon.quantum.world.vec;

import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.Point;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.util.Vec3f;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.Direction;

import java.util.Objects;

import static dev.ultreon.quantum.world.World.CS;
import static dev.ultreon.quantum.world.World.REGION_SIZE;

/**
 * Represents a block position in the world.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public final class BlockVec extends Vec3i implements Point, Cloneable {
    @Override
    public BlockVec set(Vec3i vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        return this;
    }

    /**
     * Creates a new block position from a {@link BlockVec vector}.
     *
     * @param vec the vector.
     */
    public BlockVec(BlockVec vec) {
        this(vec.x, vec.y, vec.z);
    }

    public BlockVec(int x, int y, int z) {
        super(x, y, z);
    }

    public BlockVec(double x, double y, double z) {
        super((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public BlockVec(Point point) {
        this(point.getX(), point.getY(), point.getZ());
    }

    public BlockVec(MapType data) {
        this(data.getInt("x"), data.getInt("y"), data.getInt("z"));
    }

    public BlockVec(Vec3i vec) {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    public BlockVec() {
        this(0, 0, 0);
    }

    /**
     * Creates a new block position with an offset from the current position.
     *
     * @param x the offset in the x-axis.
     * @param y the offset in the y-axis.
     * @param z the offset in the z-axis.
     * @return The new block position.
     */
    public BlockVec offset(int x, int y, int z) {
        return new BlockVec(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Converts this block position to a {@link Vec3i vector}.
     *
     * @return The vector.
     */
    public Vec3i vec() {
        return new Vec3i(this.x, this.y, this.z);
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%d", this.x, this.y, this.z);
    }

    /**
     * @return The block position below the current position.
     */
    public BlockVec below() {
        return this.offset(0, -1, 0);
    }

    /**
     * @return The block position above the current position.
     */
    public BlockVec above() {
        return this.offset(0, 1, 0);
    }

    public BlockVec offset(ChunkVec pos) {
        return this.offset(pos.getIntX() * CS, 0, pos.getIntZ() * CS);
    }

    @Override
    public double dst(Point point) {
        return this.dst(point.getIntX(), point.getIntY(), point.getIntZ());
    }

    public BlockVec offset(Direction direction) {
        return this.offset(direction.getOffset());
    }

    public BlockVec offset(Vec3i vec) {
        return new BlockVec(this.x + vec.getX(), this.y + vec.getY(), this.z + vec.getZ());
    }

    public BlockVec offset(Direction direction, int distance) {
        Vec3i offset = direction.getOffset();
        if (distance == 0) return this;

        return this.offset(offset.x * distance, offset.y * distance, offset.z * distance);
    }

    public ChunkVec chunk(RegionVec region, ChunkVecSpace space) {
        switch (space) {
            case REGION:
                int rx = region.getIntX() * REGION_SIZE;
                int ry = region.getIntY() * REGION_SIZE;
                int rz = region.getIntZ() * REGION_SIZE;

                if (region.getIntX() < 0) rx -= REGION_SIZE;
                if (region.getIntY() < 0) ry -= REGION_SIZE;
                if (region.getIntZ() < 0) rz -= REGION_SIZE;

                return new ChunkVec(rx + this.x / CS, ry + this.y / CS, rz + this.z / CS, ChunkVecSpace.REGION);
            case WORLD:
                return new ChunkVec(this.x, this.y, this.z, ChunkVecSpace.WORLD);
            default:
                throw new IllegalArgumentException();
        }
    }

    public BlockVec chunkLocal() {
        int cx = this.x % CS;
        int cy = this.y % CS;
        int cz = this.z % CS;

        if (this.x < 0 && cx != 0) cx += CS;
        if (this.y < 0 && cy != 0) cy += CS;
        if (this.z < 0 && cz != 0) cz += CS;

        return new BlockVec(cx, cy, cz);
    }

    public BlockVec sectionLocal() {
        int sx = this.x % CS;
        int sy = this.y % CS;
        int sz = this.z % CS;

        if (this.x < 0 && sx != 0) sx += CS;
        if (this.y < 0 && sy != 0) sy += CS;
        if (this.z < 0 && sz != 0) sz += CS;

        return new BlockVec(sx, sy, sz);
    }

    public BlockVec regionLocal() {
        int rx = this.x % (CS * REGION_SIZE);
        int ry = this.y % (CS * REGION_SIZE);
        int rz = this.z % (CS * REGION_SIZE);

        if (this.x < 0) rx += CS * REGION_SIZE;
        if (this.y < 0) ry += CS * REGION_SIZE;
        if (this.z < 0) rz += CS * REGION_SIZE;

        return new BlockVec(rx, ry, rz);
    }

    public BlockVec local(BlockVecSpace space) {
        switch (space) {
            case WORLD:
                return this.cpy();
            case REGION:
                return this.regionLocal();
            case CHUNK:
                return this.chunkLocal();
            case SECTION:
                return this.sectionLocal();
            default:
                throw new IllegalArgumentException();
        }
    }

    public RegionVec region() {
        int rx = this.x / (CS * REGION_SIZE);
        int ry = this.y / (CS * REGION_SIZE);
        int rz = this.z / (CS * REGION_SIZE);

        if (this.x < 0 && this.x % (CS * REGION_SIZE) != 0) rx--;
        if (this.y < 0 && this.y % (CS * REGION_SIZE) != 0) ry--;
        if (this.z < 0 && this.z % (CS * REGION_SIZE) != 0) rz--;

        return new RegionVec(rx, ry, rz);
    }

    public ChunkVec chunk() {
        int cx = this.x / CS;
        int cy = this.y / CS;
        int cz = this.z / CS;

        if (this.x < 0 && this.x % CS != 0) cx--;
        if (this.y < 0 && this.y % CS != 0) cy--;
        if (this.z < 0 && this.z % CS != 0) cz--;

        return new ChunkVec(cx, cy, cz, ChunkVecSpace.WORLD);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BlockVec) obj;
        return this.x == that.x &&
               this.y == that.y &&
               this.z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public MapType save(MapType data) {
        data.putInt("x", this.x);
        data.putInt("y", this.y);
        data.putInt("z", this.z);
        return data;
    }

    @Override
    public BlockVec add(int x, int y, int z) {
        return new BlockVec(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public BlockVec add(Vec3i vec) {
        return new BlockVec(this.x + vec.x, this.y + vec.y, this.z + vec.z);
    }

    public BlockVec add(BlockVec vec) {
        return new BlockVec(this.x + vec.x, this.y + vec.y, this.z + vec.z);
    }

    @Override
    public BlockVec sub(int x, int y, int z) {
        return new BlockVec(this.x - x, this.y - y, this.z - z);
    }

    @Override
    public BlockVec sub(Vec3i vec) {
        return new BlockVec(this.x - vec.x, this.y - vec.y, this.z - vec.z);
    }

    public BlockVec sub(BlockVec vec) {
        return new BlockVec(this.x - vec.x, this.y - vec.y, this.z - vec.z);
    }

    @Override
    public BlockVec mul(int x, int y, int z) {
        return new BlockVec(this.x * x, this.y * y, this.z * z);
    }

    @Override
    public BlockVec mul(Vec3i vec) {
        return new BlockVec(this.x * vec.x, this.y * vec.y, this.z * vec.z);
    }

    public BlockVec mul(BlockVec vec) {
        return new BlockVec(this.x * vec.x, this.y * vec.y, this.z * vec.z);
    }

    @Override
    public BlockVec div(int x, int y, int z) {
        return new BlockVec(this.x / x, this.y / y, this.z / z);
    }

    @Override
    public BlockVec div(Vec3i vec) {
        return new BlockVec(this.x / vec.x, this.y / vec.y, this.z / vec.z);
    }

    public BlockVec div(BlockVec vec) {
        return new BlockVec(this.x / vec.x, this.y / vec.y, this.z / vec.z);
    }

    @Override
    public BlockVec mod(int x, int y, int z) {
        return new BlockVec(this.x % x, this.y % y, this.z % z);
    }

    @Override
    public BlockVec mod(Vec3i vec) {
        return new BlockVec(this.x % vec.x, this.y % vec.y, this.z % vec.z);
    }

    public BlockVec mod(BlockVec vec) {
        return new BlockVec(this.x % vec.x, this.y % vec.y, this.z % vec.z);
    }

    @Override
    public BlockVec abs() {
        return new BlockVec(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));
    }

    @Override
    public BlockVec neg() {
        return new BlockVec(-this.x, -this.y, -this.z);
    }

    @Override
    public BlockVec clone() {
        return new BlockVec(this.x, this.y, this.z);
    }

    @Override
    public BlockVec cpy() {
        return new BlockVec(this.x, this.y, this.z);
    }

    @Override
    public BlockVec add(int v) {
        return new BlockVec(this.x + v, this.y + v, this.z + v);
    }

    @Override
    public BlockVec sub(int v) {
        return new BlockVec(this.x - v, this.y - v, this.z - v);
    }

    @Override
    public BlockVec mul(int v) {
        return new BlockVec(this.x * v, this.y * v, this.z * v);
    }

    @Override
    public BlockVec div(int v) {
        return new BlockVec(this.x / v, this.y / v, this.z / v);
    }

    @Override
    public BlockVec mod(int v) {
        return new BlockVec(this.x % v, this.y % v, this.z % v);
    }

    public int dot(BlockVec vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public BlockVec cross(BlockVec vec) {
        return new BlockVec(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z, this.x * vec.y - this.y * vec.x);
    }

    public BlockVec max(BlockVec vec) {
        return new BlockVec(Math.max(this.x, vec.x), Math.max(this.y, vec.y), Math.max(this.z, vec.z));
    }

    public BlockVec min(BlockVec vec) {
        return new BlockVec(Math.min(this.x, vec.x), Math.min(this.y, vec.y), Math.min(this.z, vec.z));
    }

    @Override
    public BlockVec dec() {
        return new BlockVec(this.x - 1, this.y - 1, this.z - 1);
    }

    @Override
    public BlockVec inc() {
        return new BlockVec(this.x + 1, this.y + 1, this.z + 1);
    }

    public BlockVec pow(int v) {
        return new BlockVec((int) Math.pow(this.x, v), (int) Math.pow(this.y, v), (int) Math.pow(this.z, v));
    }

    public BlockVec sqrt() {
        return new BlockVec((int) Math.sqrt(this.x), (int) Math.sqrt(this.y), (int) Math.sqrt(this.z));
    }

    @Override
    public BlockVec pow(Vec3i vec) {
        return new BlockVec((int) Math.pow(this.x, vec.x), (int) Math.pow(this.y, vec.y), (int) Math.pow(this.z, vec.z));
    }

    public BlockVec pow(Vec3f vec) {
        return new BlockVec((int) Math.pow(this.x, vec.x), (int) Math.pow(this.y, vec.y), (int) Math.pow(this.z, vec.z));
    }

    public BlockVec pow(Vec3d vec) {
        return new BlockVec((int) Math.pow(this.x, vec.x), (int) Math.pow(this.y, vec.y), (int) Math.pow(this.z, vec.z));
    }

    public BlockVec max(Vec3i vec) {
        return new BlockVec(Math.max(this.x, vec.x), Math.max(this.y, vec.y), Math.max(this.z, vec.z));
    }

    public BlockVec min(Vec3i vec) {
        return new BlockVec(Math.min(this.x, vec.x), Math.min(this.y, vec.y), Math.min(this.z, vec.z));
    }

    public BlockVec pow(double x, double y, double z) {
        return new BlockVec((int) Math.pow(this.x, x), (int) Math.pow(this.y, y), (int) Math.pow(this.z, z));
    }

    public BlockVec max(int x, int y, int z) {
        return new BlockVec(Math.max(this.x, x), Math.max(this.y, y), Math.max(this.z, z));
    }

    public BlockVec min(int x, int y, int z) {
        return new BlockVec(Math.min(this.x, x), Math.min(this.y, y), Math.min(this.z, z));
    }

    public BlockVec pow(Point point) {
        return new BlockVec((int) Math.pow(this.x, point.getX()), (int) Math.pow(this.y, point.getY()), (int) Math.pow(this.z, point.getZ()));
    }

    public BlockVec max(Point point) {
        return new BlockVec((int) Math.max(this.x, point.getX()), (int) Math.max(this.y, point.getY()), (int) Math.max(this.z, point.getZ()));
    }

    public BlockVec min(Point point) {
        return new BlockVec((int) Math.min(this.x, point.getX()), (int) Math.min(this.y, point.getY()), (int) Math.min(this.z, point.getZ()));
    }

    public BlockVec pow(double v) {
        return new BlockVec((int) Math.pow(this.x, v), (int) Math.pow(this.y, v), (int) Math.pow(this.z, v));
    }

    public BlockVec max(int v) {
        return new BlockVec(Math.max(this.x, v), Math.max(this.y, v), Math.max(this.z, v));
    }

    public BlockVec min(int v) {
        return new BlockVec(Math.min(this.x, v), Math.min(this.y, v), Math.min(this.z, v));
    }

    public double dst(BlockVec vec) {
        return super.dst(vec);
    }

    public int[] toArray() {
        return new int[]{x, y, z};
    }

    public BlockVec relative(Direction dir) {
        switch (dir) {
            case NORTH:
                return new BlockVec(this.x, this.y, this.z - 1);
            case SOUTH:
                return new BlockVec(this.x, this.y, this.z + 1);
            case EAST:
                return new BlockVec(this.x + 1, this.y, this.z);
            case WEST:
                return new BlockVec(this.x - 1, this.y, this.z);
            case UP:
                return new BlockVec(this.x, this.y + 1, this.z);
            case DOWN:
                return new BlockVec(this.x, this.y - 1, this.z);
            default:
                throw new IllegalArgumentException();
        }
    }
}
