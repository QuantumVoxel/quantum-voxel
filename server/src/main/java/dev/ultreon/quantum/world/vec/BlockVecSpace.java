package dev.ultreon.quantum.world.vec;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;
import static dev.ultreon.quantum.world.World.REGION_SIZE;

public enum BlockVecSpace {
    WORLD,
    REGION,
    CHUNK,
    SECTION;

    public void validate(int x, int y, int z) {
        switch (this) {
            case WORLD -> {
                // No validation
            }
            case REGION -> {
                if (x < 0 || x >= REGION_SIZE * CHUNK_SIZE
                        || z < 0 || z >= REGION_SIZE * CHUNK_SIZE) {
                    throw new IllegalArgumentException("Invalid region space position: " + x + ", " + y + ", " + z);
                }
            }
            case CHUNK -> {
                if (x < 0 || x >= CHUNK_SIZE
                        || z < 0 || z >= CHUNK_SIZE) {
                    throw new IllegalArgumentException("Invalid chunk space position: " + x + ", " + y + ", " + z);
                }
            }
            case SECTION -> {
                if (x < 0 || x >= CHUNK_SIZE
                        || y < 0 || y >= CHUNK_SIZE
                        || z < 0 || z >= CHUNK_SIZE) {
                    throw new IllegalArgumentException("Invalid section space position: " + x + ", " + y + ", " + z);
                }
            }

            default -> throw new IllegalArgumentException("Invalid block space: " + this);
        }
    }
}
