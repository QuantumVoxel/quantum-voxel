package dev.ultreon.quantum.world.vec;

import dev.ultreon.quantum.world.World;

public enum ChunkVecSpace {
    WORLD,
    REGION;

    public void validate(int x, int y, int z) {
        switch (this) {
            case WORLD:// World space is always valid
                break;
            case REGION:
                if (x < 0 || y < 0 || z < 0 || x >= World.REGION_SIZE || y >= World.REGION_SIZE || z >= World.REGION_SIZE)
                    throw new IllegalArgumentException("Invalid chunk position " + x + ", " + y + ", " + z);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public BlockVecSpace block() {
        switch (this) {
            case WORLD:
                return BlockVecSpace.WORLD;
            case REGION:
                return BlockVecSpace.REGION;
            default:
                throw new IllegalArgumentException();
        }
    }

    public SectionVecSpace section() {
        switch (this) {
            case WORLD:
                return SectionVecSpace.WORLD;
            case REGION:
                return SectionVecSpace.REGION;
            default:
                throw new IllegalArgumentException();
        }
    }

    public VoxelVecSpace voxel() {
        switch (this) {
            case WORLD:
                return VoxelVecSpace.WORLD;
            case REGION:
                return VoxelVecSpace.REGION;
            default:
                throw new IllegalArgumentException();
        }
    }
}
