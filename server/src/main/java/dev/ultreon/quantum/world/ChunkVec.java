package dev.ultreon.quantum.world;

import dev.ultreon.libs.commons.v0.vector.Vec2d;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents the position of a chunk in the world.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public final class ChunkVec extends Vec3i implements Comparable<ChunkVec>, Serializable {
    @Serial
    private static final long serialVersionUID = 782820744815861493L;

    /**
     * @param x The x coordinate.
     * @param z The z coordinate.
     */
    public ChunkVec(int x, int z) {
        super(x, 0, z);
    }

    /**
     * @param x The x coordinate.
     * @param z The z coordinate.
     */
    public ChunkVec(int x, int y, int z) {
        super(x, y, z);
    }

    public ChunkVec() {
        super();
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
            Integer x = ChunkVec.parseInt(split[0]);
            Integer z = ChunkVec.parseInt(split[1]);
            if (x == null) return null;
            if (z == null) return null;
            return new RegionPos(x, 0, z);
        } else if (split.length == 3) {
            Integer x = ChunkVec.parseInt(split[0]);
            Integer y = ChunkVec.parseInt(split[1]);
            Integer z = ChunkVec.parseInt(split[2]);
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
     * @param chunkVec the chunk position to be compared.
     * @return the comparison result.
     */
    @Override
    public int compareTo(ChunkVec chunkVec) {
        double dst = new Vec2d(this.x, this.z).dst(chunkVec.x, chunkVec.z);
        return dst == 0 ? 0 : dst < 0 ? -1 : 1;
    }

    @Deprecated
    public Vec2d vec() {
        return new Vec2d(this.x, this.z);
    }

    public Vec3d vec3d() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public ChunkVec offset(int x, int z) {
        return new ChunkVec(this.x + x, this.y + y, this.z + z);
    }

    public ChunkVec local() {
        return new ChunkVec(this.x & 15, this.y & 15, this.z & 15);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkVec chunkVec = (ChunkVec) o;
        return x == chunkVec.x && y == chunkVec.y && z == chunkVec.z;
    }
}
