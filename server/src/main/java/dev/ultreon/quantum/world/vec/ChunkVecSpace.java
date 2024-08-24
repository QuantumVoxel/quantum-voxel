package dev.ultreon.quantum.world.vec;

import dev.ultreon.quantum.world.World;

public enum ChunkVecSpace {
    WORLD,
    REGION;

    public void validate(int x, int y, int z) {
        switch (this) {
            case WORLD -> {
                // World space is always valid
            }
            case REGION -> {
                if (x < 0 || y < 0 || z < 0 || x >= World.REGION_SIZE || y >= World.REGION_SIZE || z >= World.REGION_SIZE)
                    throw new IllegalArgumentException("Invalid chunk position " + x + ", " + y + ", " + z);
            }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public BlockVecSpace block() {
        return switch (this) {
            case WORLD -> BlockVecSpace.WORLD;
            case REGION -> BlockVecSpace.REGION;
        };
    }

    public SectionVecSpace section() {
        return switch (this) {
            case WORLD -> SectionVecSpace.WORLD;
            case REGION -> SectionVecSpace.REGION;
        };
    }

    public VoxelVecSpace voxel() {
        return switch (this) {
            case WORLD -> VoxelVecSpace.WORLD;
            case REGION -> VoxelVecSpace.REGION;
        };
    }
}
