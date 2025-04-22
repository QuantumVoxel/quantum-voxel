package dev.ultreon.quantum.world.vec;

import static dev.ultreon.quantum.world.World.CS;
import static dev.ultreon.quantum.world.World.REGION_SIZE;

public enum BlockVecSpace {
    WORLD,
    REGION,
    CHUNK,
    SECTION;

    public void validate(int x, int y, int z) {
        switch (this) {
            case WORLD:// No validation
                break;
            case REGION:
                if (x < 0 || x >= REGION_SIZE * CS
                    || z < 0 || z >= REGION_SIZE * CS) {
                    throw new IllegalArgumentException("Invalid region space position: " + x + ", " + y + ", " + z);
                }
                break;
            case CHUNK:
                if (x < 0 || x >= CS
                    || z < 0 || z >= CS) {
                    throw new IllegalArgumentException("Invalid chunk space position: " + x + ", " + y + ", " + z);
                }
                break;
            case SECTION:
                if (x < 0 || x >= CS
                    || y < 0 || y >= CS
                    || z < 0 || z >= CS) {
                    throw new IllegalArgumentException("Invalid section space position: " + x + ", " + y + ", " + z);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid block space: " + this);
        }
    }
}
