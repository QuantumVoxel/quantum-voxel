package dev.ultreon.quantum.world;

import dev.ultreon.libs.commons.v0.vector.Vec2d;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents the position of a chunk in the world.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
@SuppressWarnings("ClassCanBeRecord")
public final class ChunkPos implements Comparable<ChunkPos>, Serializable {
    @Serial
    private static final long serialVersionUID = 782820744815861493L;
    private final int x;
    private final int y;
    private final int z;

    /**
     * @param x The x coordinate.
     * @param z The z coordinate.
     */
    public ChunkPos(int x, int z) {
        this(x, 0, z);
    }

    /**
     * @param x The x coordinate.
     * @param z The z coordinate.
     */
    public ChunkPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
    public static RegionPos parse(String s) {
        String[] split = s.split(",", 3);
        if (split.length == 2) {
            Integer x = ChunkPos.parseInt(split[0]);
            Integer z = ChunkPos.parseInt(split[1]);
            if (x == null) return null;
            if (z == null) return null;
            return new RegionPos(x, 0, z);
        } else if (split.length == 3) {
            Integer x = ChunkPos.parseInt(split[0]);
            Integer y = ChunkPos.parseInt(split[1]);
            Integer z = ChunkPos.parseInt(split[2]);
            if (x == null) return null;
            if (y == null) return null;
            if (z == null) return null;
            return new RegionPos(x, y, z);
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
        return new Vec3d(this.x * World.CHUNK_SIZE, World.WORLD_DEPTH, this.z * World.CHUNK_SIZE);
    }

    /**
     * Compare this chunk position to another.
     *
     * @param chunkPos the chunk position to be compared.
     * @return the comparison result.
     */
    @Override
    public int compareTo(ChunkPos chunkPos) {
        double dst = new Vec2d(this.x, this.z).dst(chunkPos.x, chunkPos.z);
        return dst == 0 ? 0 : dst < 0 ? -1 : 1;
    }

    public int x() {
        return this.x;
    }

    public int z() {
        return this.z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ChunkPos) obj;
        return this.x == that.x &&
               this.z == that.z;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * (31 + this.x) + this.y) + this.z;
    }

    @Deprecated
    public Vec2d vec() {
        return new Vec2d(this.x, this.z);
    }

    public Vec3d vec3d() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public ChunkPos offset(int x, int z) {
        return new ChunkPos(this.x + x, this.y + y, this.z + z);
    }
}
