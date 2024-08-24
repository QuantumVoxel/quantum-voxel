package dev.ultreon.quantum.client.world;

import dev.ultreon.quantum.util.Vec3i;

public class LightData {
    Vec3i pos = new Vec3i();
    byte level;

    public LightData(int x, int y, int z, byte level) {
        this.pos.x = x;
        this.pos.y = y;
        this.pos.z = z;
        this.level = level;
    }
}
