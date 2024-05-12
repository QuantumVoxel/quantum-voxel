package dev.ultreon.quantum.world;

import java.util.Objects;

/**
 * Represents a region position in the world.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public final class RegionPos {
    private final int x;
    private final int z;

    /**
     * @param x the x position of the region.
     * @param z the z position of the region.
     */
    public RegionPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public String toString() {
        return this.x + "," + this.z;
    }

    public int x() {
        return x;
    }

    public int z() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RegionPos) obj;
        return this.x == that.x &&
               this.z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

}
