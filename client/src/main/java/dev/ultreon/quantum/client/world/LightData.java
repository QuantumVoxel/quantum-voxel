package dev.ultreon.quantum.client.world;

import dev.ultreon.quantum.util.Vec3i;

/**
 * Represents the light data associated with a specific position.
 * This class holds the position and light level at that position.
 */
public class LightData {
    Vec3i pos = new Vec3i();
    byte level;

    /**
     * Constructs a new LightData object.
     *
     * @param x the x-coordinate of the position.
     * @param y the y-coordinate of the position.
     * @param z the z-coordinate of the position.
     * @param level the light level at the specified position.
     */
    public LightData(int x, int y, int z, byte level) {
        this.pos.x = x;
        this.pos.y = y;
        this.pos.z = z;
        this.level = level;
    }
}
