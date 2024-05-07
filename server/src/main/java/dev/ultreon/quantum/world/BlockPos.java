package dev.ultreon.quantum.world;

import dev.ultreon.libs.commons.v0.vector.Vec3i;
import org.checkerframework.common.reflection.qual.NewInstance;

/**
 * Represents a block position in the world.
 *
 * @param x
 * @param y
 * @param z
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public record BlockPos(int x, int y, int z) {
    /**
     * Creates a new block position at the given coordinates.
     *
     * @param x the x-coordinate.
     * @param y the y-coordinate.
     * @param z the z-coordinate.
     */
    public BlockPos(double x, double y, double z) {
        this((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    /**
     * Creates a new block position at {@code 0,0,0}.
     */
    public BlockPos() {
        this(0, 0, 0);
    }

    /**
     * Creates a new block position from a {@link Vec3i vector}.
     *
     * @param vec the vector.
     */
    public BlockPos(Vec3i vec) {
        this(vec.x, vec.y, vec.z);
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
    public BlockPos offset(int x, int y, int z) {
        return new BlockPos(x, y, z);
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
        return "%d,%d,%d".formatted(this.x, this.y, this.z);
    }

    /**
     * @return The block position below the current position.
     */
    public BlockPos below() {
        return this.offset(0, -1, 0);
    }

    /**
     * @return The block position above the current position.
     */
    public BlockPos above() {
        return this.offset(0, -1, 0);
    }

    public BlockPos offset(ChunkPos pos) {
        return this.offset(pos.x() * World.CHUNK_SIZE, 0, pos.z() * World.CHUNK_SIZE);
    }

    public BlockPos offset(CubicDirection direction) {
        return this.offset(direction.getOffset());
    }

    public BlockPos offset(CubicDirection direction, int distance) {
        BlockPos offset = direction.getOffset();

        return this.offset(offset.x * distance, offset.y * distance, offset.z * distance);
    }

    private BlockPos offset(BlockPos offset) {
        return this.offset(x + offset.x, y + offset.y, z +  offset.z);
    }
}
