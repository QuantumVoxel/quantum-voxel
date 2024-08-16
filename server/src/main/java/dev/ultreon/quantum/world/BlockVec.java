package dev.ultreon.quantum.world;

import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.ubo.types.MapType;
import org.checkerframework.common.reflection.qual.NewInstance;

import java.util.Objects;

/**
 * Represents a block position in the world.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public final class BlockVec {
    private final int x;
    private final int y;
    private final int z;

    /**
     * @param x
     * @param y
     * @param z
     */
    public BlockVec(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a new block position at the given coordinates.
     *
     * @param x the x-coordinate.
     * @param y the y-coordinate.
     * @param z the z-coordinate.
     */
    public BlockVec(double x, double y, double z) {
        this((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    /**
     * Creates a new block position at {@code 0,0,0}.
     */
    public BlockVec() {
        this(0, 0, 0);
    }

    /**
     * Creates a new block position from a {@link Vec3i vector}.
     *
     * @param vec the vector.
     */
    public BlockVec(Vec3i vec) {
        this(vec.x, vec.y, vec.z);
    }

    public BlockVec(MapType data) {
        this(data.getInt("x"), data.getInt("y"), data.getInt("z"));
    }

    /**
     * Creates a new block position with an offset from the current position.
     *
     * @param x the offset in the x-axis.
     * @param y the offset in the y-axis.
     * @param z the offset in the z-axis.
     * @return The new block position.
     */
    @NewInstance
    public BlockVec offset(int x, int y, int z) {
        return new BlockVec(x, y, z);
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
        return this.offset(0, -1, 0);
    }

    public BlockVec offset(ChunkVec pos) {
        return this.offset(pos.getX() * World.CHUNK_SIZE, 0, pos.getZ() * World.CHUNK_SIZE);
    }

    public BlockVec offset(CubicDirection direction) {
        return this.offset(direction.getOffset());
    }

    public BlockVec offset(CubicDirection direction, int distance) {
        BlockVec offset = direction.getOffset();

        return this.offset(offset.x * distance, offset.y * distance, offset.z * distance);
    }

    private BlockVec offset(BlockVec offset) {
        return this.offset(x + offset.x, y + offset.y, z + offset.z);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
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
}
