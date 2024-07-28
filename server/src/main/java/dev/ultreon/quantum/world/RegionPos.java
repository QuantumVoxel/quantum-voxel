package dev.ultreon.quantum.world;

import java.util.Objects;

/**
 * Represents a region position in the world.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public record RegionPos(int x, int y, int z) {
    public RegionPos(int rx, int rz) {
        this(rx, 0, rz);
    }

    @Override
    public String toString() {
        return this.x + "," + this.y + "," + this.z;
    }
}
